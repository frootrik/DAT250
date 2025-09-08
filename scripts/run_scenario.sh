#!/usr/bin/env bash
set -euo pipefail

# Config
BASE_URL="${BASE_URL:-http://localhost:8080}"

curl_json() {
  local method="$1"; shift
  local url="$1"; shift
  local data="${1-}"
  if [[ -n "${data}" ]]; then
    curl -sS -X "${method}" "${url}" -H 'Content-Type: application/json' -d "${data}"
  else
    curl -sS -X "${method}" "${url}"
  fi
}

hr() { printf "\n%s\n\n" "------------------------------------------------------------"; }

echo "Base URL: ${BASE_URL}"
hr

echo "1) Create users"
U1=$(curl_json POST "${BASE_URL}/users" '{"name":"Alice","email":"alice@example.com"}' | jq -r .id)
U2=$(curl_json POST "${BASE_URL}/users" '{"name":"Bob","email":"bob@example.com"}' | jq -r .id)
echo "User1: $U1"
echo "User2: $U2"
echo "All users:"; curl_json GET "${BASE_URL}/users" | jq
hr

echo "2) Create poll with options"
POLL=$(curl_json POST "${BASE_URL}/polls" \
  "{\"ownerUserId\":\"${U1}\",\"question\":\"Best language?\",\"options\":[\"Kotlin\",\"Java\",\"Go\"]}" \
  | jq -r .id)
echo "Poll: $POLL"
echo "Polls:"; curl_json GET "${BASE_URL}/polls" | jq
hr

echo "3) Read poll options"
curl_json GET "${BASE_URL}/polls/${POLL}" | jq '.options'
OPT1=$(curl_json GET "${BASE_URL}/polls/${POLL}" | jq -r '.options[0].id')
OPT2=$(curl_json GET "${BASE_URL}/polls/${POLL}" | jq -r '.options[1].id')
OPT3=$(curl_json GET "${BASE_URL}/polls/${POLL}" | jq -r '.options[2].id')
echo "OPT1=$OPT1  OPT2=$OPT2  OPT3=$OPT3"
hr

echo "4) User2 votes, then changes vote"
curl_json POST "${BASE_URL}/polls/${POLL}/votes" "{\"userId\":\"${U2}\",\"optionId\":\"${OPT1}\"}" | jq
curl_json POST "${BASE_URL}/polls/${POLL}/votes" "{\"userId\":\"${U2}\",\"optionId\":\"${OPT2}\"}" | jq
echo "Votes now:"; curl_json GET "${BASE_URL}/polls/${POLL}/votes" | jq
hr

echo "5) Update poll question"
curl_json PUT "${BASE_URL}/polls/${POLL}" '{"question":"Best programming language in 2025?"}' | jq '.question'
hr

echo "6) Try deleting an option that has votes (should be 400)"
curl -sS -o /dev/null -w "HTTP %{\nhttp_code}\n" -X DELETE "${BASE_URL}/polls/${POLL}/options/${OPT2}"
echo "Delete unused option (should be 204)"
curl -sS -o /dev/null -w "HTTP %{\nhttp_code}\n" -X DELETE "${BASE_URL}/polls/${POLL}/options/${OPT3}"
hr

echo "7) Clear all votes (should be 204)"
curl -sS -o /dev/null -w "HTTP %{\nhttp_code}\n" -X DELETE "${BASE_URL}/polls/${POLL}/votes"
echo "Votes after clear (should be []):"
curl_json GET "${BASE_URL}/polls/${POLL}/votes" | jq
hr

echo "8) Delete poll (should be 204)"
curl -sS -o /dev/null -w "HTTP %{\nhttp_code}\n" -X DELETE "${BASE_URL}/polls/${POLL}"

echo "Check votes after poll deletion (should be 404):"
curl -sS -o /dev/null -w "HTTP %{\nhttp_code}\n" "${BASE_URL}/polls/${POLL}/votes"
hr

echo "Done ✅"

