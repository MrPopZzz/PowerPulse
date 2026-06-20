create table `user` (
	`id` bigint not null auto_increment primary key,
	`firstName` varchar(100) not null,
	`lastName` varchar(100),
	`email` varchar(255) not null,
	`address` text,
	`alerting` tinyint(1) not null default 0,
	`energy_alerting_threshold` double not null default 0,
	unique key `ind_user_email` (`email`)
) ENGINE=InnoDB default CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;