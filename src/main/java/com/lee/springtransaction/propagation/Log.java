package com.lee.springtransaction.propagation;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Getter
@Setter
@Entity
public class Log {

    @Id
    @GeneratedValue
    private Long id;
    private String message;

    public Log(){}

    public Log(String message) {
        this.message = message;
    }
}
