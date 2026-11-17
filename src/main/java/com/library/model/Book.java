package com.library.model;

import java.math.BigDecimal;

public record Book(long id, String isbn, String title, String author, String publisher,
                   String category, BigDecimal price, int quantity, int availableQuantity) {
}
