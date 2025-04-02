package org.example;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.*;

import java.io.FileInputStream;
import java.util.concurrent.CountDownLatch;

public class FirebaseManager {
    private static DatabaseReference rankingRef;
    private static DatabaseReference usersRef;


    public static void initFirebase() throws Exception {
        FileInputStream serviceAccount = new FileInputStream("C:/Users/marcm/IdeaProjects/Quiz/src/main/resources/serviceAccountKey.json");

        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setDatabaseUrl("https://juegom06-3f3a0-default-rtdb.europe-west1.firebasedatabase.app/")
                .build();

        FirebaseApp.initializeApp(options);
        rankingRef = FirebaseDatabase.getInstance().getReference("ranking");
        usersRef = FirebaseDatabase.getInstance().getReference("users");
    }

    public static void checkOrCreateUser(String username, Runnable onSuccess) {
        usersRef.child(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if(!snapshot.exists()) {
                    usersRef.child(username).setValueAsync("usuario_registrado");
                }
                onSuccess.run();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                System.err.println("Error al verificar usuario: " + error.getMessage());
            }
        });
    }



    public static void updateScore(String playerId, int newScore, CountDownLatch latch) {
        DatabaseReference userScoreRef = rankingRef.child(playerId);

        userScoreRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Integer currentScore = mutableData.getValue(Integer.class);
                if (currentScore == null || newScore > currentScore) {
                    mutableData.setValue(newScore);
                }
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError error, boolean committed, DataSnapshot dataSnapshot) {
                if (error != null) {
                    System.err.println("Error actualizando puntuación: " + error.getMessage());
                }
                latch.countDown();
            }
        });
    }

    public static void listenRanking() {
        rankingRef.orderByValue().limitToLast(10).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                System.out.println("\n=== TOP 10 JUGADORES ===");
                int position = 1;
                for(DataSnapshot ds : snapshot.getChildren()) {
                    String player = ds.getKey();
                    Long score = ds.getValue(Long.class);
                    System.out.println(position++ + ". " + player + " - Puntuación: " + score);
                }
                System.out.println("=========================\n");
            }

            @Override
            public void onCancelled(DatabaseError error) {
                System.err.println("Error en ranking: " + error.getMessage());
            }
        });
    }

}
