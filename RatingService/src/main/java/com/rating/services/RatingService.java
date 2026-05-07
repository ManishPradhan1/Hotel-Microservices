package com.rating.services;

import java.util.List;

import com.rating.entities.Rating;

public interface RatingService {
	
	//create
	Rating create(Rating rating);
	
	
	//get all ratings
	List<Rating> getRatings();
	
	
	//get all ratings by user id
	List<Rating> getRatingByUserId(String userId);
	
	
	//get all ratings by hotel
	List<Rating> getRatingByHotelId(String hotelId);
	
}
