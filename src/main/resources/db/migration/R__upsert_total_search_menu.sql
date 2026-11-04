INSERT INTO total_search_keyword (keyword, keyword_type, target_key)
VALUES
    ('메인', 'MENU', 'HOME'),
    ('커뮤니티', 'MENU', 'COMMUNITY'),
    ('경매장', 'MENU', 'AUCTION'),
    ('거래 내역', 'MENU', 'AUCTION_HISTORY'),
    ('실시간 거래', 'MENU', 'AUCTION_REALTIME'),
    ('시세 정보', 'MENU', 'STATISTICS'),
    ('뿔피리', 'MENU', 'HORN_BUGLE'),
    ('도감', 'MENU', 'CODEX'),
    ('아이템 도감', 'MENU', 'ITEM_INFO'),
    ('마이페이지', 'MENU', 'MYPAGE'),
    ('로그인', 'MENU', 'SIGN_IN')
ON DUPLICATE KEY UPDATE
    keyword = VALUES(keyword),
    updated_at = CURRENT_TIMESTAMP(6);

DELETE FROM total_search_keyword
WHERE keyword_type = 'MENU'
  AND target_key NOT IN (
      'HOME', 'COMMUNITY', 'AUCTION', 'AUCTION_HISTORY', 'AUCTION_REALTIME',
      'STATISTICS', 'HORN_BUGLE', 'CODEX', 'ITEM_INFO', 'MYPAGE', 'SIGN_IN'
  );
