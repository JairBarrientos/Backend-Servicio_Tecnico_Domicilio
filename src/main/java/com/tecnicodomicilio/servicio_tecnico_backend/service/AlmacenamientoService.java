package com.tecnicodomicilio.servicio_tecnico_backend.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

@Service
public class AlmacenamientoService {

    private static final long TAMANO_MAXIMO = 8L * 1024 * 1024;

    private static final Map<String, String> EXTENSIONES_IMAGEN = Map.of(
            "image/webp", ".webp",
            "image/jpeg", ".jpg",
            "image/png", ".png");

    private final Path carpetaServicios = Paths.get("uploads", "servicios");
    private final Path carpetaDocumentos = Paths.get("private-uploads", "documentos");

    public String guardarImagenServicio(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new RuntimeException("Selecciona una imagen");
        }

        String tipo = archivo.getContentType();
        String extension = tipo != null ? EXTENSIONES_IMAGEN.get(tipo) : null;
        if (extension == null) {
            throw new RuntimeException("Formato no permitido. Usa WebP, JPG o PNG");
        }

        try {
            Files.createDirectories(carpetaServicios);
            String nombre = UUID.randomUUID() + extension;
            Files.copy(archivo.getInputStream(), carpetaServicios.resolve(nombre), StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/servicios/" + nombre;
        } catch (IOException e) {
            throw new RuntimeException("No se pudo guardar la imagen");
        }
    }

    public String guardarDocumento(MultipartFile archivo, String prefijo, boolean esPdf) {
        if (archivo == null || archivo.isEmpty()) {
            throw new RuntimeException("Selecciona un archivo");
        }
        if (archivo.getSize() > TAMANO_MAXIMO) {
            throw new RuntimeException("El archivo supera el máximo de 8 MB");
        }

        String tipo = archivo.getContentType();
        String extension;
        if (esPdf) {
            extension = "application/pdf".equals(tipo) ? ".pdf" : null;
            if (extension == null) {
                throw new RuntimeException("El CV debe ser un archivo PDF");
            }
        } else {
            extension = tipo != null ? EXTENSIONES_IMAGEN.get(tipo) : null;
            if (extension == null) {
                throw new RuntimeException("La foto debe ser JPG, PNG o WebP");
            }
        }

        try {
            Files.createDirectories(carpetaDocumentos);
            String nombre = prefijo + "-" + UUID.randomUUID() + extension;
            Files.copy(archivo.getInputStream(), carpetaDocumentos.resolve(nombre), StandardCopyOption.REPLACE_EXISTING);
            return nombre;
        } catch (IOException e) {
            throw new RuntimeException("No se pudo guardar el documento");
        }
    }

    public byte[] leerDocumento(String nombre) {
        Path base = carpetaDocumentos.toAbsolutePath().normalize();
        Path archivo = base.resolve(nombre).normalize();

        if (!archivo.startsWith(base) || !Files.exists(archivo)) {
            throw new RuntimeException("Documento no encontrado");
        }

        try {
            return Files.readAllBytes(archivo);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo leer el documento");
        }
    }

    public void eliminarDocumento(String nombre) {
        if (nombre == null || nombre.isBlank()) return;

        try {
            Path base = carpetaDocumentos.toAbsolutePath().normalize();
            Path archivo = base.resolve(nombre).normalize();
            if (archivo.startsWith(base)) {
                Files.deleteIfExists(archivo);
            }
        } catch (IOException ignorada) {
        }
    }

    public String tipoContenido(String nombre) {
        String minuscula = nombre.toLowerCase();
        if (minuscula.endsWith(".pdf")) return "application/pdf";
        if (minuscula.endsWith(".png")) return "image/png";
        if (minuscula.endsWith(".webp")) return "image/webp";
        return "image/jpeg";
    }

        public String guardarFotoPerfil(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new RuntimeException("Selecciona una foto");
        }
        if (archivo.getSize() > TAMANO_MAXIMO) {
            throw new RuntimeException("La foto supera el máximo de 8 MB");
        }

        String tipo = archivo.getContentType();
        String extension = tipo != null ? EXTENSIONES_IMAGEN.get(tipo) : null;
        if (extension == null) {
            throw new RuntimeException("La foto debe ser JPG, PNG o WebP");
        }

        try {
            Path carpeta = Paths.get("uploads", "perfiles");
            Files.createDirectories(carpeta);
            String nombre = UUID.randomUUID() + extension;
            Files.copy(archivo.getInputStream(), carpeta.resolve(nombre), StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/perfiles/" + nombre;
        } catch (IOException e) {
            throw new RuntimeException("No se pudo guardar la foto");
        }
    }
}