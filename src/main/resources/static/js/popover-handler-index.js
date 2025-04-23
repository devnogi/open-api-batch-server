const PopoverHandler = {
  POPOVER_BTN_CLASS: 'popover-btn',
  POPOVER_CLASS: 'popover',
  SHOW_DELAY_MS: 50,
  HIDE_DELAY_MS: 100,

  activePopovers: new Map(),

  init() {
    document.addEventListener('DOMContentLoaded', () => {
      const buttons = document.querySelectorAll(`.${this.POPOVER_BTN_CLASS}`);
      buttons.forEach((button) => this.setupButton(button));
    });
  },

  setupButton(button) {
    const id = button.dataset.id;
    const contentEl = document.getElementById(`popover-content-${id}`);
    if (!contentEl) return;

    // 가짜 요소 생성
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

    this.activePopovers.set(button, {
      popover,
      fakeTarget,
      isInside: false,
    });

    button.addEventListener('mousemove', (e) => {
      this.updateFakeTargetPosition(button, e);
    });

    button.addEventListener('mouseenter', (e) => this.showPopover(button, e));
    button.addEventListener('mouseleave', () => this.tryHidePopover(button));
  },

  updateFakeTargetPosition(button, e) {
    const popoverData = this.activePopovers.get(button);
    if (!popoverData) return;

    const { fakeTarget } = popoverData;
    fakeTarget.style.left = `${e.clientX + 10}px`; // 마우스 오른쪽
    fakeTarget.style.top = `${e.clientY - 10}px`;  // 마우스 약간 위
  },

  showPopover(button, e) {
    const popoverData = this.activePopovers.get(button);
    if (!popoverData) return;

    this.updateFakeTargetPosition(button, e); // 위치 업데이트
    popoverData.popover.show();

    setTimeout(() => {
      const popoverEl = document.querySelector(`.${this.POPOVER_CLASS}`);
      if (!popoverEl) return;

      this.setupPopoverEvents(popoverEl, button);
    }, this.SHOW_DELAY_MS);
  },

  setupPopoverEvents(popoverEl, button) {
    const popoverData = this.activePopovers.get(button);
    if (!popoverData) return;

    popoverEl.addEventListener('mouseenter', () => {
      popoverData.isInside = true;
    });

    popoverEl.addEventListener('mouseleave', () => {
      popoverData.isInside = false;
      this.tryHidePopover(button);
    });

    popoverEl.addEventListener('wheel', this.preventBodyScroll(popoverEl), {
      passive: false,
    });
  },

  preventBodyScroll(el) {
    return function (e) {
      const { scrollTop, scrollHeight, clientHeight } = el;
      const delta = e.deltaY;
      const isScrollingDown = delta > 0;
      const isAtBottom = scrollTop + clientHeight >= scrollHeight;
      const isAtTop = scrollTop <= 0;

      if ((isScrollingDown && isAtBottom) || (!isScrollingDown && isAtTop)) {
        e.preventDefault();
      }
      e.stopPropagation();
    };
  },

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

PopoverHandler.init();
