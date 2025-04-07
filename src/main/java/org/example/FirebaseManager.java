package org.example;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.*;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class FirebaseManager {
    private static DatabaseReference rankingRef;
    private static DatabaseReference usersRef;

    public interface RankingListener {
        void onRankingUpdate(List<RankingEntry> ranking);
    }

    public static void initFirebase() throws Exception {
        FileInputStream serviceAccount = new FileInputStream("src/main/resources/serviceAccountKey.json");

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
                if (!snapshot.exists()) {
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
            public Transaction.Result doTransaction(MutableData currentData) {
                Integer currentScore = currentData.getValue(Integer.class);
                if (currentScore == null || newScore > currentScore) {
                    currentData.setValue(newScore);
                }
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(DatabaseError error, boolean committed, DataSnapshot snapshot) {
                if (error != null) {
                    System.err.println("Error al actualizar puntuación: " + error.getMessage());
                }
                latch.countDown();
            }
        });
    }

    public static void listenRanking(RankingListener listener) {
        rankingRef.orderByValue().limitToLast(10).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<RankingEntry> ranking = new ArrayList<>();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String player = ds.getKey();
                    Long score = ds.getValue(Long.class);
                    ranking.add(new RankingEntry(player, score != null ? score.intValue() : 0));
                }
                ranking.sort((a, b) -> Integer.compare(b.getScore(), a.getScore()));
                listener.onRankingUpdate(ranking);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                System.err.println("Error al escuchar ranking: " + error.getMessage());
            }
        });
    }
}
