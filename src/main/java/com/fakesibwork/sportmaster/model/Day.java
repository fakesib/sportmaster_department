package com.fakesibwork.sportmaster.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "day")
@NoArgsConstructor
@AllArgsConstructor
public class Day {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private long chat;
    private String role;
    private int store;
    private String name;
    private int here;
    private int delivery;
    private int mobile;
    private int email;
    private int fast;
}
