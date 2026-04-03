#!/bin/bash
# test-api.sh - Linux/Mac API test script for Zorvyn
# Usage:
#   API_BASE_URL=http://localhost:8081 ./test-api.sh
#   ./test-api.sh

set -euo pipefail

API_BASE_URL=${API_BASE_URL:-http://localhost:8081}
TEST_USER=${TEST_USER:-testuser}
TEST_EMAIL=${TEST_EMAIL:-testuser@example.com}
TEST_PASS=${TEST_PASS:-test@123}

function call_api() {
  local method=$1
  local path=$2
  local data=${3:-}
  local token=${4:-}

  local url="$API_BASE_URL$path"
  local headers=(-H "Content-Type: application/json")
  if [[ -n "$token" ]]; then
    headers+=( -H "Authorization: Bearer $token" )
  fi

  if [[ -n "$data" ]]; then
    echo "curl -s -X $method $url with body $data"
    response=$(curl -s -w "\n%{http_code}" -X "$method" "$url" "${headers[@]}" -d "$data")
  else
    echo "curl -s -X $method $url"
    response=$(curl -s -w "\n%{http_code}" -X "$method" "$url" "${headers[@]}")
  fi

  http_code=$(echo "$response" | tail -n1)
  payload=$(echo "$response" | sed '$d')

  if [[ "$http_code" -ge 400 ]]; then
    echo "Error [$http_code]: $payload"
    exit 1
  fi

  echo "$payload"
}

printf "\n1) Signup user\n"
signup=$(call_api POST /api/auth/signup "{\"username\":\"$TEST_USER\",\"email\":\"$TEST_EMAIL\",\"password\":\"$TEST_PASS\"}")
printf "Signup: %s\n" "$signup"

printf "\n2) Login user\n"
login=$(call_api POST /api/auth/login "{\"username\":\"$TEST_USER\",\"password\":\"$TEST_PASS\"}")
printf "Login: %s\n" "$login"

token=$(echo "$login" | jq -r '.token // empty')
if [[ -z "$token" ]]; then
  echo "Login token missing"
  exit 1
fi

printf "\n3) Get records\n"
records=$(call_api GET /api/records '' "$token")
printf "Records: %s\n" "$records"

printf "\n4) Create record\n"
create=$(call_api POST /api/records "{\"type\":\"INCOME\",\"category\":\"Salary\",\"amount\":5000,\"description\":\"Test salary\",\"date\":\"$(date +%F)\"}" "$token")
printf "Created: %s\n" "$create"

recordId=$(echo "$create" | jq -r '.id // ._id // empty')
if [[ -z "$recordId" ]]; then
  echo "Created record id missing"
  exit 1
fi

printf "\n5) Get dashboard\n"
dash=$(call_api GET /api/records/dashboard '' "$token")
printf "Dashboard: %s\n" "$dash"

printf "\n6) Filter Income\n"
filtered=$(call_api GET '/api/records/filter?type=INCOME' '' "$token")
printf "Filtered: %s\n" "$filtered"

printf "\n7) Update record\n"
updated=$(call_api PUT "/api/records/$recordId" "{\"type\":\"INCOME\",\"category\":\"Updated Salary\",\"amount\":6000,\"description\":\"Updated test\",\"date\":\"$(date +%F)\"}" "$token")
printf "Updated: %s\n" "$updated"

printf "\n8) Get record by id\n"
byid=$(call_api GET "/api/records/$recordId" '' "$token")
printf "By ID: %s\n" "$byid"

printf "\n9) Delete record\n"
call_api DELETE "/api/records/$recordId" '' "$token"
printf "Deleted %s\n" "$recordId"

printf "\n10) Confirm records list\n"
final=$(call_api GET /api/records '' "$token")
printf "Final records: %s\n" "$final"

printf "\nAPI test complete\n"