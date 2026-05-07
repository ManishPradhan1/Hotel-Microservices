package com.rating.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rating.entities.Rating;
import com.rating.repository.RatingRepository;
import com.rating.services.RatingService;

@Service
public class RatingServiceImpl implements RatingService {

	@Autowired
	private RatingRepository repository;

	@Override
	public Rating create(Rating rating) {
		return repository.save(rating);
	}

	@Override
	public List<Rating> getRatings() {
		return repository.findAll();
	}

	@Override
	public List<Rating> getRatingByUserId(String userId) {
		return repository.findByUserId(userId);
	}

	@Override
	public List<Rating> getRatingByHotelId(String hotelId) {
		return repository.findByHotelId(hotelId);
	}

}
