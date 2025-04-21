const PopoverHandler = {
  POPOVER_BTN_CLASS: 'popover-btn',
  POPOVER_CONTENT_CLASS: 'popover-content',
  POPOVER_CLASS: 'popover',
  SHOW_DELAY_MS: 50,
  HIDE_DELAY_MS: 100,

  activePopovers: new Map(), // key: button element, value: popover instance
  isInsidePopoverMap: new Map(), // key: button element, value: boolean

  init() {
    document.addEventListener('DOMContentLoaded', () => {
      const buttons = document.querySelectorAll(`.${this.POPOVER_BTN_CLASS}`);

      buttons.forEach((button) => {
        const contentId = button.dataset.popoverContentId;
        const contentEl = contentId
          ? document.getElementById(contentId)
          : button.querySelector(`.${this.POPOVER_CONTENT_CLASS}`);

        if (!contentEl) return;

        const popoverInstance = new bootstrap.Popover(button, {
          content: contentEl.innerHTML,
          html: true,
          trigger: 'manual',
          placement: 'right',
        });

        this.activePopovers.set(button, popoverInstance);
        this.isInsidePopoverMap.set(button, false);

        this.bindButtonEvents(button);
      });
    });
  },

  bindButtonEvents(button) {
    button.addEventListener('mouseenter', () => this.showPopover(button));
    button.addEventListener('mouseleave', () => this.tryHidePopover(button));
  },

  showPopover(button) {
    const popoverInstance = this.activePopovers.get(button);
    if (!popoverInstance) return;

    popoverInstance.show();

    setTimeout(() => {
      const popovers = document.querySelectorAll(`.${this.POPOVER_CLASS}`);
      const latestPopover = Array.from(popovers).pop(); // 가장 최근에 띄워진 popover로 가정

      if (!latestPopover) return;

      this.setupPopoverEvents(latestPopover, button);
    }, this.SHOW_DELAY_MS);
  },

  setupPopoverEvents(popoverEl, button) {
    popoverEl.addEventListener('mouseenter', () => {
      this.isInsidePopoverMap.set(button, true);
    });

    popoverEl.addEventListener('mouseleave', () => {
      this.isInsidePopoverMap.set(button, false);
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
      const isInsidePopover = this.isInsidePopoverMap.get(button);
      if (!isInsidePopover && !button.matches(':hover')) {
        this.activePopovers.get(button)?.hide();
      }
    }, this.HIDE_DELAY_MS);
  }
};

// 실행
PopoverHandler.init();
