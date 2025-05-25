package configurations.cripted;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashPassword {

    public static String gerarHash(String senha) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(senha.getBytes());
            return bytesParaHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Erro ao gerar hash SHA-256", e);
        }
    }

    public static boolean verificarSenha(String senhaDigitada, String hashSalvo) {
        return gerarHash(senhaDigitada).equals(hashSalvo);
    }

    private static String bytesParaHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
