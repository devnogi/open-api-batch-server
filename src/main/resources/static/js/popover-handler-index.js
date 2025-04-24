const PopoverHandler = {
  // 버튼 클래스명 및 팝오버 클래스 정의
  POPOVER_BTN_CLASS: 'popover-btn',
  POPOVER_CLASS: 'popover',
  SHOW_DELAY_MS: 50, // 필요시 사용 가능한 딜레이 (현재는 사용 안 함)

  // 버튼마다 연결된 팝오버 정보를 저장하는 Map
  activePopovers: new Map(),

  // 현재 화면에 표시되고 있는 팝오버 엘리먼트
  currentPopoverEl: null,

  // 페이지 로드 후 초기화
  init() {
    document.addEventListener('DOMContentLoaded', () => {
      // 모든 팝오버 버튼을 선택
      const buttons = document.querySelectorAll(`.${this.POPOVER_BTN_CLASS}`);
      buttons.forEach((button) => this.setupButton(button)); // 각 버튼에 팝오버 설정
      this.setupGlobalWheelHandler(); // 마우스 휠 스크롤 이벤트 설정
    });
  },

  // 팝오버 버튼에 이벤트 및 팝오버 설정
  setupButton(button) {
    const id = button.dataset.id; // 버튼에 있는 data-id 속성 값
    const contentEl = document.getElementById(`popover-content-${id}`); // 팝오버 내용 요소

    if (!contentEl) return; // 내용이 없으면 생략

    // 실제로 화면에 표시되지 않는, 팝오버의 위치를 제어할 가짜 타겟(div) 생성
    const fakeTarget = document.createElement('div');
    fakeTarget.style.position = 'absolute';
    fakeTarget.style.zIndex = '-1'; // 화면 뒤로 보내기
    fakeTarget.style.pointerEvents = 'none'; // 마우스 이벤트 무시
    document.body.appendChild(fakeTarget); // body에 추가

    // Bootstrap Popover 객체 생성
    const popover = new bootstrap.Popover(fakeTarget, {
      content: contentEl.innerHTML, // 내용은 HTML 문자열
      html: true, // HTML 해석 가능
      trigger: 'manual', // 수동으로 표시 제어
      placement: 'right', // 오른쪽에 표시
    });

    // 마우스 이동 시 팝오버 위치 업데이트용 핸들러
    const mouseMoveHandler = (e) => {
      this.updateFakeTargetPosition(button, e); // 마우스 위치로 가짜 타겟 이동
      const popoverData = this.activePopovers.get(button);
      if (popoverData?.popover?._popper) {
        popoverData.popover._popper.update(); // 팝오버 위치 재계산
      }
    };

    // 버튼에 해당하는 팝오버 관련 정보 저장
    this.activePopovers.set(button, {
      popover,
      fakeTarget,
      mouseMoveHandler,
    });

    // 마우스를 올렸을 때 처리
    button.addEventListener('mouseenter', (e) => {
      this.hideAllPopovers(button); // 현재 버튼 제외하고 나머지 팝오버 숨김
      button.addEventListener('mousemove', mouseMoveHandler); // 마우스 움직임 추적 시작
      this.showPopover(button, e); // 팝오버 표시
    });

    // 마우스를 벗어났을 때 처리
    button.addEventListener('mouseleave', () => {
      const data = this.activePopovers.get(button);
      if (data?.mouseMoveHandler) {
        button.removeEventListener('mousemove', data.mouseMoveHandler); // 추적 중단
      }
      this.hidePopoverImmediately(button); // 해당 팝오버 즉시 닫기
    });
  },

  // 가짜 타겟 위치를 마우스 커서 근처로 이동
  updateFakeTargetPosition(button, e) {
    const popoverData = this.activePopovers.get(button);
    if (!popoverData) return;

    const { fakeTarget } = popoverData;
    fakeTarget.style.left = `${e.clientX + 10}px`; // 마우스 우측 10px
    fakeTarget.style.top = `${e.clientY - 10}px`; // 마우스 상단 10px
  },

  // 팝오버 표시
  showPopover(button, e) {
    const popoverData = this.activePopovers.get(button);
    if (!popoverData) return;

    this.updateFakeTargetPosition(button, e); // 위치 먼저 갱신
    popoverData.popover.show(); // 팝오버 표시

    // 표시된 팝오버의 DOM 엘리먼트를 저장 (휠 이벤트 처리를 위해)
    const popoverEl = popoverData.popover._getTipElement?.();
    if (popoverEl) {
      this.currentPopoverEl = popoverEl;
    }
  },

  // 팝오버를 즉시 닫음
  hidePopoverImmediately(button) {
    const popoverData = this.activePopovers.get(button);
    if (!popoverData) return;

    popoverData.popover?.hide();
    this.currentPopoverEl = null;
  },

  // 현재 버튼을 제외하고 모든 팝오버 닫기
  hideAllPopovers(excludeButton = null) {
    for (const [button, data] of this.activePopovers.entries()) {
      if (button !== excludeButton) {
        data.popover?.hide();
      }
    }
    console.log("excludeButton");
    this.currentPopoverEl = null;
  },

  // 휠 스크롤 이벤트 처리 → 팝오버 안의 내용만 스크롤되도록 설정
  setupGlobalWheelHandler() {
    document.addEventListener(
        'wheel',
        (e) => {
          if (!this.currentPopoverEl) return; // 팝오버 없으면 무시

          const popoverBody = this.currentPopoverEl.querySelector('.popover-body'); // 내용 영역
          if (!popoverBody) return;

          const { scrollTop, scrollHeight, clientHeight } = popoverBody;
          const delta = e.deltaY; // 스크롤 방향 및 거리
          const isScrollingDown = delta > 0;
          const isAtBottom = scrollTop + clientHeight >= scrollHeight;
          const isAtTop = scrollTop <= 0;

          // 상단/하단에서 더 이상 스크롤할 수 없으면 body 스크롤 막기
          if ((isScrollingDown && isAtBottom) || (!isScrollingDown && isAtTop)) {
            e.preventDefault();
          } else {
            e.preventDefault();
            popoverBody.scrollTop += delta; // 팝오버 내부만 스크롤 이동
          }
        },
        { passive: false } // preventDefault를 위해 반드시 false
    );
  },
};

// 초기화 실행
PopoverHandler.init();
