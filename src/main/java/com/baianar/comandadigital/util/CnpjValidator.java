package com.baianar.comandadigital.util;

/** RN07: validação de CNPJ pelo algoritmo oficial dos dígitos verificadores. */
public final class CnpjValidator {

    private CnpjValidator() {
    }

    public static boolean isValid(String cnpjInput) {
        if (cnpjInput == null) {
            return false;
        }
        String cnpj = cnpjInput.replaceAll("[^0-9]", "");

        if (cnpj.length() != 14 || cnpj.chars().distinct().count() == 1) {
            return false;
        }

        int[] pesos1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int[] pesos2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

        int digito1 = calcularDigito(cnpj.substring(0, 12), pesos1);
        if (digito1 != Character.getNumericValue(cnpj.charAt(12))) {
            return false;
        }

        int digito2 = calcularDigito(cnpj.substring(0, 13), pesos2);
        return digito2 == Character.getNumericValue(cnpj.charAt(13));
    }

    private static int calcularDigito(String base, int[] pesos) {
        int soma = 0;
        for (int i = 0; i < base.length(); i++) {
            soma += Character.getNumericValue(base.charAt(i)) * pesos[i];
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }
}
