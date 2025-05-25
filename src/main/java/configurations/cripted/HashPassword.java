package configurations.cripted;

import org.mindrot.jbcrypt.BCrypt;

public class HashPassword {

    public static String gerarHash(String senhaPura) {
        return BCrypt.hashpw(senhaPura, BCrypt.gensalt(12));
    }

    // Verifica se a senha fornecida corresponde ao hash armazenado
    public static boolean verificarSenha(String senhaPura, String hashArmazenado) {
        return BCrypt.checkpw(senhaPura, hashArmazenado);
    }
}
