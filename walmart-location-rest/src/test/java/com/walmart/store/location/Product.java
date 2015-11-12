package com.walmart.store.location;

import com.github.reinert.jjschema.Attributes;

import java.math.BigDecimal;
import java.util.List;

@Attributes(title="Product", description="A product from Acme's catalog")
public  class Product {
    @Attributes(required=true, description="The unique identifier for a product")
    private long id;
    @Attributes(required=true, description="Name of the product")
    private String name;
    @Attributes(required=true, minimum=0, exclusiveMinimum=true)
    private BigDecimal price;
    @Attributes(minItems=1,uniqueItems=true)
    private List<String> tags;

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public BigDecimal getPrice() {
        return price;
    }
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    public List<String> getTags() {
        return tags;
    }
    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}
