#!/bin/bash

set -e

REPORT_FILE="build/reports/jacoco/test/jacocoTestReport.xml"
BADGE_FILE="badges/coverage.svg"

mkdir -p badges

if [ ! -f "$REPORT_FILE" ]; then
  echo "❌ Report not found: $REPORT_FILE"
  exit 1
fi

# awk로 missed, covered 값 추출
read missed covered < <(awk -F'"' '/<counter type="LINE"/ {print $4, $6}' "$REPORT_FILE")

total=$((missed + covered))
percent=$(awk "BEGIN { printf \"%.2f\", ($covered / $total) * 100 }")

# 색상 결정
if (( $(echo "$percent >= 90" | bc -l) )); then
  color="4c1" # brightgreen
elif (( $(echo "$percent >= 75" | bc -l) )); then
  color="dfb317" # yellow
else
  color="e05d44" # red
fi

# SVG badge 생성
cat > "$BADGE_FILE" <<EOF
<svg xmlns="http://www.w3.org/2000/svg" width="130" height="20">
  <linearGradient id="b" x2="0" y2="100%">
    <stop offset="0" stop-color="#eee" stop-opacity=".7"/>
    <stop offset="1" stop-opacity=".7"/>
  </linearGradient>
  <mask id="a">
    <rect width="130" height="20" rx="3" fill="#fff"/>
  </mask>
  <g mask="url(#a)">
    <rect width="75" height="20" fill="#555"/>
    <rect x="75" width="55" height="20" fill="#$color"/>
    <rect width="130" height="20" fill="url(#b)"/>
  </g>
  <g fill="#fff" text-anchor="middle"
      font-family="DejaVu Sans,Verdana,Geneva,sans-serif" font-size="11">
    <text x="37.5" y="15" fill="#010101" fill-opacity=".3">coverage</text>
    <text x="37.5" y="14">coverage</text>
    <text x="101.5" y="15" fill="#010101" fill-opacity=".3">${percent}%</text>
    <text x="101.5" y="14">${percent}%</text>
  </g>
</svg>
EOF

echo "✅ Coverage badge generated: ${percent}% -> $BADGE_FILE"
