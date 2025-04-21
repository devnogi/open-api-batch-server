const PopoverHandler = {
  POPOVER_BTN_ID: 'popoverBtn',
  POPOVER_CONTENT_ID: 'popoverContent',
  POPOVER_CLASS: 'popover',
  SHOW_DELAY_MS: 50,
  HIDE_DELAY_MS: 100,

  isInsidePopover: false,
  popoverInstance: null,

  init() {
    document.addEventListener('DOMContentLoaded', () => {
      const button = document.getElementById(this.POPOVER_BTN_ID);
      const popoverContent = document.getElementById(this.POPOVER_CONTENT_ID);

      if (!button || !popoverContent) return;

      this.createPopover(button, popoverContent.innerHTML);
      this.bindButtonEvents(button);
    });
  },

  createPopover(button, content) {
    this.popoverInstance = new bootstrap.Popover(button, {
      content,
      html: true,
      trigger: 'manual',
      placement: 'right',
    });
  },

  bindButtonEvents(button) {
    button.addEventListener('mouseenter', () => this.showPopover(button));
    button.addEventListener('mouseleave', () => this.tryHidePopover(button));
  },

  showPopover(button) {
    if (!this.popoverInstance) return;

    this.popoverInstance.show();

    setTimeout(() => {
      const popoverEl = document.querySelector(`.${this.POPOVER_CLASS}`);
      if (!popoverEl) return;

      this.setupPopoverEvents(popoverEl, button);
    }, this.SHOW_DELAY_MS);
  },

  setupPopoverEvents(popoverEl, button) {
    popoverEl.addEventListener('mouseenter', () => {
      this.isInsidePopover = true;
    });

    popoverEl.addEventListener('mouseleave', () => {
      this.isInsidePopover = false;
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
    setTimeout(() => {
      if (!this.isInsidePopover && !button.matches(':hover')) {
        this.popoverInstance?.hide();
      }
    }, this.HIDE_DELAY_MS);
  }
};

// 실행
PopoverHandler.init();
