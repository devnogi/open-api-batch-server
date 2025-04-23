const PopoverHandler = {
  POPOVER_BTN_CLASS: 'popover-btn',
  POPOVER_CLASS: 'popover',
  SHOW_DELAY_MS: 50,

  activePopovers: new Map(),
  currentPopoverEl: null,

  init() {
    document.addEventListener('DOMContentLoaded', () => {
      const buttons = document.querySelectorAll(`.${this.POPOVER_BTN_CLASS}`);
      buttons.forEach((button) => this.setupButton(button));
      this.setupGlobalWheelHandler();
    });
  },

  setupButton(button) {
    const id = button.dataset.id;
    const contentEl = document.getElementById(`popover-content-${id}`);
    if (!contentEl) return;

    const fakeTarget = document.createElement('div');
    fakeTarget.style.position = 'absolute';
    fakeTarget.style.zIndex = '-1';
    fakeTarget.style.pointerEvents = 'none';
    document.body.appendChild(fakeTarget);

    const popover = new bootstrap.Popover(fakeTarget, {
      content: contentEl.innerHTML,
      html: true,
      trigger: 'manual',
      placement: 'right',
    });

    const mouseMoveHandler = (e) => {
      this.updateFakeTargetPosition(button, e);
      const popoverData = this.activePopovers.get(button);
      if (popoverData?.popover?._popper) {
        popoverData.popover._popper.update();
      }
    };

    this.activePopovers.set(button, {
      popover,
      fakeTarget,
      mouseMoveHandler,
    });

    button.addEventListener('mouseenter', (e) => {
      this.hideAllPopovers(); // 빠르게 이동해도 기존 팝오버 정리
      button.addEventListener('mousemove', mouseMoveHandler);
      this.showPopover(button, e);
    });

    button.addEventListener('mouseleave', () => {
      const data = this.activePopovers.get(button);
      if (data?.mouseMoveHandler) {
        button.removeEventListener('mousemove', data.mouseMoveHandler);
      }
      this.hidePopoverImmediately(button);
    });
  },

  updateFakeTargetPosition(button, e) {
    const popoverData = this.activePopovers.get(button);
    if (!popoverData) return;

    const { fakeTarget } = popoverData;
    fakeTarget.style.left = `${e.clientX + 10}px`;
    fakeTarget.style.top = `${e.clientY - 10}px`;
  },

  showPopover(button, e) {
    const popoverData = this.activePopovers.get(button);
    if (!popoverData) return;

    this.updateFakeTargetPosition(button, e);
    popoverData.popover.show();

    // 팝오버 엘리먼트를 즉시 가져와 currentPopoverEl로 설정
    const popoverEl = popoverData.popover._getTipElement?.();
    if (popoverEl) {
      this.currentPopoverEl = popoverEl;
    }
  },

  hidePopoverImmediately(button) {
    const popoverData = this.activePopovers.get(button);
    if (!popoverData) return;

    popoverData.popover?.hide();
    this.currentPopoverEl = null;
  },

  hideAllPopovers() {
    for (const [button, data] of this.activePopovers.entries()) {
      data.popover?.hide();
    }
    this.currentPopoverEl = null;
  },

  setupGlobalWheelHandler() {
    document.addEventListener(
      'wheel',
      (e) => {
        if (!this.currentPopoverEl) return;

        const popoverBody = this.currentPopoverEl.querySelector('.popover-body');
        if (!popoverBody) return;

        const { scrollTop, scrollHeight, clientHeight } = popoverBody;
        const delta = e.deltaY;
        const isScrollingDown = delta > 0;
        const isAtBottom = scrollTop + clientHeight >= scrollHeight;
        const isAtTop = scrollTop <= 0;

        if ((isScrollingDown && isAtBottom) || (!isScrollingDown && isAtTop)) {
          e.preventDefault(); // body 스크롤 방지
        } else {
          e.preventDefault(); // 항상 body는 막음
          popoverBody.scrollTop += delta; // 팝오버 내용 스크롤
        }
      },
      { passive: false }
    );
  },
};

PopoverHandler.init();
