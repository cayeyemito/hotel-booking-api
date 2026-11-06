package io.github.cayeyemito.hotel_booking_api;

import org.springframework.boot.SpringApplication;

public class TestHotelBookingApiApplication {

	public static void main(String[] args) {
		SpringApplication.from(HotelBookingApiApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
