create table `device` (
	`id` bigint not null auto_increment,
	`name` varchar(255),
	`type` varchar(50),
	`location` varchar(255),
	`user_id` bigint,
	primary key (`id`),
	key `idx_device_user_id` (`user_id`),
	constraint `fk_device_user`
		foreign key (`user_id`) references `user` (`id`)
		on delete cascade
) ENGINE=InnoDB default CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;