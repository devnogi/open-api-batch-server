#!/bin/bash

################################################################################
# Swap Memory Setup Script
#
# 목적: Linux 서버에 영구적인 Swap 메모리를 설정합니다.
#
# Swap 메모리란?
# - 물리 RAM이 부족할 때 디스크 공간을 가상 메모리로 사용하는 기술
# - RAM이 꽉 찼을 때 덜 사용되는 메모리를 디스크로 이동(swap out)
# - 필요할 때 다시 RAM으로 로드(swap in)
#
# 왜 필요한가?
# 1. OOM(Out Of Memory) Killer 방지: RAM 부족 시 프로세스가 강제 종료되는 것을 방지
# 2. 시스템 안정성: 메모리 부족 시에도 시스템이 멈추지 않고 느리더라도 동작
# 3. SSH 접속 불가 방지: 메모리 부족으로 SSH 데몬이 종료되는 것을 방지
#
# 주의사항:
# - Swap은 디스크 I/O이므로 RAM보다 훨씬 느립니다 (100~1000배)
# - 근본적 해결책은 RAM 증설이지만, 임시 방편으로 효과적입니다
#
# 사용법:
# sudo bash setup-swap.sh
################################################################################

set -e  # 에러 발생 시 스크립트 중단

# 색상 정의 (터미널 출력용)
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Root 권한 확인
if [ "$EUID" -ne 0 ]; then
    echo -e "${RED}Error: This script must be run as root${NC}"
    echo "Please run: sudo bash $0"
    exit 1
fi

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  Swap Memory Setup Script${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""

# 1. 현재 메모리 상태 확인
echo -e "${YELLOW}[Step 1] Checking current memory status...${NC}"
free -h
echo ""

# 2. 기존 Swap 파일 확인
if [ -f /swapfile ]; then
    echo -e "${YELLOW}[Warning] /swapfile already exists${NC}"
    read -p "Do you want to recreate it? (y/n): " -n 1 -r
    echo ""
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        echo "Removing existing swap..."
        swapoff /swapfile 2>/dev/null || true  # 에러 무시
        rm -f /swapfile
    else
        echo "Exiting..."
        exit 0
    fi
fi

# 3. Swap 크기 설정 (기본 1GB)
SWAP_SIZE="${SWAP_SIZE:-1G}"
echo -e "${YELLOW}[Step 2] Creating ${SWAP_SIZE} swap file...${NC}"

# fallocate로 빠르게 파일 생성 (dd보다 훨씬 빠름)
# fallocate: 디스크에 연속된 공간을 즉시 할당
if ! fallocate -l $SWAP_SIZE /swapfile; then
    # fallocate 실패 시 dd로 대체 (일부 파일시스템은 fallocate 미지원)
    echo "fallocate failed, using dd instead..."
    dd if=/dev/zero of=/swapfile bs=1M count=1024 status=progress
fi

# 4. 파일 권한 설정 (보안상 root만 읽기/쓰기 가능)
echo -e "${YELLOW}[Step 3] Setting file permissions (600)...${NC}"
chmod 600 /swapfile
ls -lh /swapfile

# 5. Swap 파일 시스템 생성
echo -e "${YELLOW}[Step 4] Creating swap filesystem...${NC}"
mkswap /swapfile

# 6. Swap 활성화
echo -e "${YELLOW}[Step 5] Enabling swap...${NC}"
swapon /swapfile

# 7. /etc/fstab에 추가 (재부팅 후에도 자동 활성화)
echo -e "${YELLOW}[Step 6] Adding to /etc/fstab for persistence...${NC}"
if ! grep -q "/swapfile" /etc/fstab; then
    # 기존 fstab 백업
    cp /etc/fstab /etc/fstab.backup.$(date +%Y%m%d_%H%M%S)
    echo "/swapfile none swap sw 0 0" >> /etc/fstab
    echo "Added to /etc/fstab"
else
    echo "Already exists in /etc/fstab"
fi

# 8. Swap 성능 최적화 (선택사항)
echo -e "${YELLOW}[Step 7] Optimizing swap settings...${NC}"

# vm.swappiness: Swap 사용 빈도 조절 (0-100)
# - 0: 가능한 한 Swap 사용 안 함 (메모리 부족 시에만)
# - 10: 권장값 (메모리가 적은 서버용, 디스크 I/O 최소화)
# - 60: 기본값 (데스크톱용)
# - 100: 적극적으로 Swap 사용
sysctl vm.swappiness=10

# vm.vfs_cache_pressure: 파일시스템 캐시 회수 빈도 (기본 100)
# - 낮을수록 캐시를 더 오래 유지 (I/O 성능 향상)
# - 50: 캐시를 더 오래 보관
sysctl vm.vfs_cache_pressure=50

# 영구 적용 (재부팅 후에도 유지)
if ! grep -q "vm.swappiness" /etc/sysctl.conf; then
    echo "vm.swappiness=10" >> /etc/sysctl.conf
fi
if ! grep -q "vm.vfs_cache_pressure" /etc/sysctl.conf; then
    echo "vm.vfs_cache_pressure=50" >> /etc/sysctl.conf
fi

# 9. 최종 확인
echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  Swap Setup Completed Successfully!${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo -e "${YELLOW}Current Memory Status:${NC}"
free -h
echo ""
echo -e "${YELLOW}Swap Details:${NC}"
swapon --show
echo ""
echo -e "${GREEN}✓ Swap is now active and will persist after reboot${NC}"
echo -e "${GREEN}✓ Swappiness set to 10 (conservative swap usage)${NC}"
echo -e "${GREEN}✓ VFS cache pressure set to 50 (better I/O caching)${NC}"
echo ""
echo -e "${YELLOW}Tip: Monitor swap usage with 'free -h' or 'swapon --show'${NC}"
