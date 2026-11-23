package com.tecnicodomicilio.servicio_tecnico_backend.config;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class ManejadorErrores {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<String> archivoDemasiadoGrande(MaxUploadSizeExceededException e) {
        return ResponseEntity.status(413).body("El archivo es demasiado pesado (máximo 10 MB)");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> datosConFormatoIncorrecto(HttpMessageNotReadableException e) {
        return ResponseEntity.badRequest().body("Los datos enviados no tienen el formato correcto");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> datosInvalidos(DataIntegrityViolationException e) {
        return ResponseEntity.badRequest().body("Los datos ingresados no son válidos o ya existen");
    }
}