const functions = require('firebase-functions');
const admin = require('firebase-admin');

admin.initializeApp(functions.config().firebase);

exports.sendChallengeMessage = functions.database.ref('/Challenges/{challengeId}').onCreate((snapshot, context) => {
    console.log("sendChallengeMessage function invoked"+snapshot.val());
    const challenge = snapshot.val();
    var challengerUserUid = challenge.challengerUserUid;
    var challengedUserUid = challenge.challengedUserUid;
    var status = challenge.status;

    return admin.database().ref("/token/" + challengedUserUid).once('value').then((snap) => {
        const fcmToken = snap.val();
        console.log("Token:", fcmToken);
        const payload = {
            data: {
                challengerUserUid: challengerUserUid,
                status: status,
                type: "Challenge"
            }
        };

        return admin.messaging().sendToDevice(fcmToken, payload).then((response) => {
            console.log("Sucessfully sent", response);
        }).catch((error) => {
            console.log("Error occured", error);
        });
    })
        .catch((error) => {
            console.log("Error", error);
        });
});

exports.sendFollowingMessage = functions.database.ref('/followers/{userUid}/{followingUserUid}')
    .onCreate((snapshot, context) => {

        console.log("sendFollowingMessage function invoked");
        const followedUserUid = context.params.userUid;
        const data = snapshot.val();
        const followingUserUid = data.user_id;
        return admin.database().ref("/token/" + followedUserUid).once('value').then((snap) => {
            const fcmToken = snap.val();
            const payload = {
                data: {
                    type: "Following",
                    followerUserUid: followingUserUid
                }
            };
            return admin.messaging().sendToDevice(fcmToken, payload).then((response) => {
                console.log("Successfully sent", response);
            })
                .catch((error) => {
                    console.log("Following Message MessageLevelError", error);
                });

        })
            .catch((error) => {
                console.log("Following Message tokenLevel Error", error);
            });
    });
