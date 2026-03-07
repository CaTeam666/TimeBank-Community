ALTER TABLE `tb_exchange_order` ADD COLUMN `order_no` varchar(50) NOT NULL COMMENT '订单编号';
ALTER TABLE `tb_exchange_order` ADD COLUMN `amount` int(11) NOT NULL COMMENT '消耗时间币';
ALTER TABLE `tb_exchange_order` ADD UNIQUE INDEX `uni_order_no` (`order_no`);
