package com.linkme.LinkMe;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class FirebaseQuerry {


    public static void getData(final FirestoreCallback firestoreCallback, String ExistingPhone) {

        /*

          FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference().child("User");
        Query getUserInfo = databaseReference.orderByChild("name").equalTo(ExistingPhone);
        getUserInfo.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                System.out.println("Inside Data snapshot");
                for (DataSnapshot user : dataSnapshot.getChildren()) {
                    User currentUser = user.getValue(User.class);
                    firestoreCallback.OncallBack(currentUser);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
         */


        final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference dbref = firebaseDatabase.getReference("User");

        Query query = dbref.orderByChild("phonenumber").equalTo(ExistingPhone);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    if (userSnapshot.exists()) {
                        User currentUser = userSnapshot.getValue(User.class);
                        firestoreCallback.OncallBack(currentUser);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }


    public static void getKey(final FirestoreCallback firestoreCallback, String ExistingPhone){
        final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference dbref = firebaseDatabase.getReference("User");

        Query query = dbref.orderByChild("phonenumber").equalTo(ExistingPhone);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChildren()){
                    firestoreCallback.OncallBackKey(null);
                }
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    if (userSnapshot.exists()) {
                         firestoreCallback.OncallBackKey(userSnapshot.getKey());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }



    public interface FirestoreCallback {
        void OncallBack(User currentUser);
        void OncallBackKey(String key);
    }


}

/*
 ValueEventListener postListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            // Get Post object and use the values to update the UI
            Post post = dataSnapshot.getValue(Post.class);
            // ...
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            // Getting Post failed, log a message
            Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            // ...
        }
    };

 */



