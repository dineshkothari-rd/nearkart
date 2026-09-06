#!/bin/sh
set -eu

for tool in curl jq; do command -v "$tool" >/dev/null 2>&1 || { echo "Missing required command: $tool" >&2; exit 1; }; done
: "${ADMIN_EMAIL:?Set ADMIN_EMAIL to the bootstrap admin email}"
: "${ADMIN_PASSWORD:?Set ADMIN_PASSWORD to the bootstrap admin password}"

base=${API_BASE_URL:-http://localhost:8080/api/v1}
suffix="$(date +%s)-$$"
password='JourneyTestPassword!'

post() {
	if [ -n "$2" ]; then
		curl --fail-with-body --silent --show-error -H 'Content-Type: application/json' -H "Authorization: Bearer $2" -d "$3" "$base$1"
	else
		curl --fail-with-body --silent --show-error -H 'Content-Type: application/json' -d "$3" "$base$1"
	fi
}
patch() { curl --fail-with-body --silent --show-error -X PATCH -H 'Content-Type: application/json' -H "Authorization: Bearer $2" -d "$3" "$base$1"; }
status() { curl --silent --output /dev/null --write-out '%{http_code}' "$@"; }

admin=$(post /auth/login '' "$(jq -nc --arg email "$ADMIN_EMAIL" --arg password "$ADMIN_PASSWORD" '{email:$email,password:$password}')")
admin_token=$(printf '%s' "$admin" | jq -er '.data.accessToken')

owner=$(post /auth/register '' "$(jq -nc --arg email "owner-$suffix@journey.test" --arg password "$password" '{email:$email,password:$password,displayName:"Journey Owner",accountType:"STORE_OWNER"}')")
owner_token=$(printf '%s' "$owner" | jq -er '.data.accessToken')
store=$(post /owner/stores "$owner_token" '{"name":"Journey Test Mart","description":"Automated journey","phone":"+919876543210","timezone":"Asia/Kolkata","location":{"addressLine":"MI Road","locality":"C-Scheme","city":"Jaipur","state":"Rajasthan","postalCode":"302001","latitude":26.912400,"longitude":75.787300}}')
store_id=$(printf '%s' "$store" | jq -er '.data.id')
patch "/admin/stores/$store_id/approval" "$admin_token" '{"decision":"APPROVE"}' >/dev/null
patch "/owner/stores/$store_id" "$owner_token" '{"description":"Automated journey verified"}' >/dev/null
curl --fail-with-body --silent --show-error -X PUT -H 'Content-Type: application/json' -H "Authorization: Bearer $owner_token" -d '{"hours":[{"weekday":1,"opensAt":"09:00","closesAt":"21:00","closed":false}]}' "$base/owner/stores/$store_id/hours" >/dev/null

suggestion=$(curl --fail-with-body --silent --show-error "$base/search?q=Amul%20Butter")
variant_id=$(printf '%s' "$suggestion" | jq -er '.data[0].variantId')
product_id=$(printf '%s' "$suggestion" | jq -er '.data[0].productId')
listing=$(post "/owner/stores/$store_id/products" "$owner_token" "$(jq -nc --arg variantId "$variant_id" '{variantId:$variantId,amount:285,currency:"INR",quantity:5,availability:"AVAILABLE"}')")
listing_id=$(printf '%s' "$listing" | jq -er '.data.id')
patch "/owner/store-products/$listing_id/inventory" "$owner_token" '{"quantity":2,"availability":"LOW_STOCK","expectedVersion":0}' >/dev/null
test "$(status -X PATCH -H 'Content-Type: application/json' -H "Authorization: Bearer $owner_token" -d '{"quantity":1,"availability":"LOW_STOCK","expectedVersion":0}' "$base/owner/store-products/$listing_id/inventory")" = 409
patch "/owner/store-products/$listing_id/price" "$owner_token" '{"amount":286,"currency":"INR","expectedVersion":0}' >/dev/null

customer=$(post /auth/register '' "$(jq -nc --arg email "customer-$suffix@journey.test" --arg password "$password" '{email:$email,password:$password,displayName:"Journey Customer",accountType:"CUSTOMER"}')")
customer_token=$(printf '%s' "$customer" | jq -er '.data.accessToken')
curl --fail-with-body --silent --show-error -H "Authorization: Bearer $customer_token" "$base/search/nearby?q=Amul%20Butter&latitude=26.9124&longitude=75.7873&radiusKm=5" | jq -e --arg id "$store_id" '.data.content | any(.storeId == $id)' >/dev/null
post /favorites "$customer_token" "$(jq -nc --arg targetId "$product_id" '{type:"PRODUCT",targetId:$targetId}')" >/dev/null
review=$(post /reviews "$customer_token" "$(jq -nc --arg storeId "$store_id" '{storeId:$storeId,rating:4,text:"Automated journey review"}')")
review_id=$(printf '%s' "$review" | jq -er '.data.id')
report=$(post /reports "$customer_token" "$(jq -nc --arg storeId "$store_id" --arg storeProductId "$listing_id" '{storeId:$storeId,storeProductId:$storeProductId,type:"WRONG_PRICE",details:"Automated journey report"}')")
report_id=$(printf '%s' "$report" | jq -er '.data.id')

test "$(status -H "Authorization: Bearer $owner_token" "$base/admin/dashboard")" = 403
test "$(status -H "Authorization: Bearer $customer_token" "$base/owner/stores/$store_id")" = 403
patch "/admin/reviews/$review_id" "$admin_token" '{"status":"APPROVED"}' >/dev/null
patch "/admin/reports/$report_id" "$admin_token" '{"status":"RESOLVED"}' >/dev/null
curl --fail-with-body --silent --show-error -H "Authorization: Bearer $owner_token" "$base/owner/stores/$store_id/analytics" | jq -e '.data.products == 1 and .data.lowStock == 1 and .data.searches >= 1' >/dev/null
curl --fail-with-body --silent --show-error -H "Authorization: Bearer $admin_token" "$base/admin/dashboard" | jq -e '.data.users >= 3 and .data.stores >= 1 and .data.products >= 1' >/dev/null

echo "NearKart API journey passed: customer, owner, admin, RBAC, and stale-write conflict."
