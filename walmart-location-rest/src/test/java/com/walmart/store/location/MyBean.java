package com.walmart.store.location;


import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;


public  class MyBean {

    @JsonProperty(required = true)
    @Length(min = 6, max = 30)
    private String secret = "secret";

    @Size(min = 0, max = 13)
    private int age;

    private String obvious = "obvious";

    public String getSecret() {
        return secret;
    }

    public void setSecret(String s) {
        secret = s;
    }

    public String getObvious() {
        return obvious;
    }

    public void setObvious(String s) {
        obvious = s;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}