/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gmail.anselm92.georeferencing;

/**
 * Class that contains constants that describe the state of an opened image 
 * @author Anselm
 */
public class ProcessingState {
     static final int META_DATA_MISSING =0 ;
     static final int PROCESSED=1;
     static final int WAITING=2;
     static final int PROCESSING=3;
     static final int META_DATA_WRONG =4;
}
