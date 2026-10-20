/*
 * SPDX-License-Identifier: PolyForm-Noncommercial-1.0.0
 * Copyright (c) 2026 Muha
 * Commercial use only with prior permission. See LICENSE.md.
 */

package de.muha.minecraftshop.common;

import de.muha.minecraftshop.item.DuplicateItemException;
import de.muha.minecraftshop.item.ItemNotFoundException;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Liefert Fehler als RFC 9457 ProblemDetail. Validierungsfehler (400) übernimmt
 * bereits {@link ResponseEntityExceptionHandler}.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ItemNotFoundException.class)
    public ProblemDetail handleItemNotFound(ItemNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "Item not found", ex.getMessage());
    }

    @ExceptionHandler(DuplicateItemException.class)
    public ProblemDetail handleDuplicateItem(DuplicateItemException ex) {
        return problem(HttpStatus.CONFLICT, "Item already exists", ex.getMessage());
    }

    /** z.B. ?sort=foo – unbekanntes Sortierfeld ist ein Client-Fehler, kein 500. */
    @ExceptionHandler(PropertyReferenceException.class)
    public ProblemDetail handleInvalidSort(PropertyReferenceException ex) {
        return problem(HttpStatus.BAD_REQUEST, "Invalid sort property", ex.getMessage());
    }

    private static ProblemDetail problem(HttpStatus status, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        return problem;
    }
}
