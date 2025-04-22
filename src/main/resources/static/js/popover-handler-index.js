const PopoverHandler = {
  // 클래스명 및 설정값 정의
  POPOVER_BTN_CLASS: 'popover-btn',   // 팝오버를 트리거하는 버튼 클래스
  POPOVER_CLASS: 'popover',           // Bootstrap 팝오버의 클래스
  SHOW_DELAY_MS: 50,                  // 팝오버 보여주는 지연 시간
  HIDE_DELAY_MS: 100,                 // 팝오버 숨김 지연 시간

  activePopovers: new Map(),          // 각 버튼에 대한 팝오버 인스턴스와 상태 저장

  // 초기화: DOM이 로드되면 모든 버튼에 팝오버 설정
  init() {
    document.addEventListener('DOMContentLoaded', () => {
      const buttons = document.querySelectorAll(`.${this.POPOVER_BTN_CLASS}`);
      buttons.forEach((button) => this.setupButton(button));
    });
  },

  // 각 버튼에 팝오버를 연결하고 이벤트 리스너 추가
  setupButton(button) {
    const id = button.dataset.id;
    const contentEl = document.getElementById(`popover-content-${id}`);
    if (!contentEl) return;

    const popover = new bootstrap.Popover(button, {
      content: contentEl.innerHTML,
      html: true,
      trigger: 'manual', // 수동으로 show/hide 컨트롤
      placement: 'right', // 팝오버가 오른쪽에 표시됨
    });

    this.activePopovers.set(button, { popover, isInside: false });

    button.addEventListener('mouseenter', () => this.showPopover(button));
    button.addEventListener('mouseleave', () => this.tryHidePopover(button));
  },

  // 팝오버를 보여주고 내부에 마우스 이벤트 바인딩
  showPopover(button) {
    const popoverData = this.activePopovers.get(button);
    if (!popoverData) return;

    popoverData.popover.show();

    // 약간의 딜레이 후 팝오버 요소가 DOM에 생성된 뒤 내부 이벤트 등록
    setTimeout(() => {
      const popoverEl = document.querySelector(`.${this.POPOVER_CLASS}`);
      if (!popoverEl) return;

      this.setupPopoverEvents(popoverEl, button);
    }, this.SHOW_DELAY_MS);
  },

  // 팝오버 안에서 마우스가 들어가거나 나갈 때의 동작 설정
  setupPopoverEvents(popoverEl, button) {
    const popoverData = this.activePopovers.get(button);
    if (!popoverData) return;

    // 팝오버 내부에 마우스가 있을 때 플래그 변경
    popoverEl.addEventListener('mouseenter', () => {
      popoverData.isInside = true;
    });

    // 팝오버를 벗어났을 때 숨기기 시도
    popoverEl.addEventListener('mouseleave', () => {
      popoverData.isInside = false;
      this.tryHidePopover(button);
    });

    // 팝오버 내부에서 스크롤 시 body 스크롤 방지
    popoverEl.addEventListener('wheel', this.preventBodyScroll(popoverEl), {
      passive: false,
    });
  },

  // 팝오버 내부 스크롤 제한 함수
  preventBodyScroll(el) {
    return function (e) {
      const { scrollTop, scrollHeight, clientHeight } = el;
      const delta = e.deltaY;
      const isScrollingDown = delta > 0;
      const isAtBottom = scrollTop + clientHeight >= scrollHeight;
      const isAtTop = scrollTop <= 0;

      if ((isScrollingDown && isAtBottom) || (!isScrollingDown && isAtTop)) {
        e.preventDefault(); // 바깥쪽으로 스크롤 전파 방지
      }
      e.stopPropagation();
    };
  },

  // 팝오버 숨기기 시도 (버튼과 팝오버 모두에서 마우스가 벗어났을 때)
  tryHidePopover(button) {
    const popoverData = this.activePopovers.get(button);
    if (!popoverData) return;

    setTimeout(() => {
      if (!popoverData.isInside && !button.matches(':hover')) {
        popoverData.popover?.hide();
      }
    }, this.HIDE_DELAY_MS);
  }
};

// 실행
PopoverHandler.init();
