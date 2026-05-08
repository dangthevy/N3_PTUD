IF NOT EXISTS (
  SELECT * FROM sys.columns
  WHERE object_id = OBJECT_ID('KhachHang')
  AND name = 'ngayThem'
)
ALTER TABLE KhachHang ADD ngayThem DATE NULL;
GO