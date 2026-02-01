package com.orbit.product_service.model;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Table(name="sub_categories")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubCategory {
	@Id
	Integer id;
	@Column(name = "sub_category_name")
	String name;

	@OneToMany(mappedBy = "subCategory", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Product> products;

	@ManyToOne
	@JoinColumn(name = "category_id", nullable = false)
	Category category;
}
