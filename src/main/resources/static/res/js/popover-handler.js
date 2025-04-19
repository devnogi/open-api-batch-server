(() => {
  document.addEventListener('DOMContentLoaded', initPopoverHandler);

  const POPOVER_BTN_ID = 'popoverBtn';
  const POPOVER_CONTENT_ID = 'popoverContent';
  const POPOVER_CLASS = 'popover';
  const SHOW_DELAY_MS = 50;
  const HIDE_DELAY_MS = 100;

  let isInsidePopover = false;
  let popoverInstance = null;

  function initPopoverHandler() {
    const button = document.getElementById(POPOVER_BTN_ID);
    const popoverContent = document.getElementById(POPOVER_CONTENT_ID);

    if (!button || !popoverContent) return;

    createPopover(button, popoverContent.innerHTML);
    bindButtonEvents(button);
  }

  function createPopover(button, content) {
    popoverInstance = new bootstrap.Popover(button, {
      content,
      html: true,
      trigger: 'manual',
      placement: 'right',
    });
  }

  function bindButtonEvents(button) {
    button.addEventListener('mouseenter', () => showPopover(button));
    button.addEventListener('mouseleave', tryHidePopover);
  }

  function showPopover(button) {
    if (!popoverInstance) return;
    popoverInstance.show();

    setTimeout(() => {
      const popoverEl = document.querySelector(`.${POPOVER_CLASS}`);
      if (!popoverEl) return;

      setupPopoverEvents(popoverEl, button);
    }, SHOW_DELAY_MS);
  }

  function setupPopoverEvents(popoverEl, button) {
    popoverEl.addEventListener('mouseenter', () => {
      isInsidePopover = true;
    });

    popoverEl.addEventListener('mouseleave', () => {
      isInsidePopover = false;
      tryHidePopover(button);
    });

    popoverEl.addEventListener('wheel', preventBodyScroll(popoverEl), { passive: false });
  }

  function preventBodyScroll(el) {
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
  }

  function tryHidePopover(button) {
    setTimeout(() => {
      if (!isInsidePopover && !button.matches(':hover')) {
        popoverInstance?.hide();
      }
    }, HIDE_DELAY_MS);
  }
})();
