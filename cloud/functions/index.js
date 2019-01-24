const functions = require('firebase-functions');
const admin = require('firebase-admin');

admin.initializeApp(functions.config().firebase);

exports.sendMessage = functions.database.ref('/Challenges/{challengeId}').onUpdate((change, context) => {
    console.log("update triggered");
    const datasnapshot = change.after;
    const challenge = datasnapshot.val();
    const status = challenge.status;
    const challengedUserUid = challenge.challengedUserUid;
    const challengerUserUid = challenge.challengerUserUid;
    if (status == "ACCEPTED") {
        const postKey = challenge.postKey;
        const payload = {
            data: {
                challengedUserUid: challengedUserUid,
                status: status,
                postKey: postKey,
                type:"Challenge"
            }
        }
    } else if (status == "REJECTED") {
        const payload = {
            data: {
                challengedUserUid: challengedUserUid,
                status: status,
                type:"Challenge"
            }
        }
    }
    return admin.database().ref("/token/" + challengerUserUid).once('value').then((snap) => {
        const fcmToken = snap.val();
        console.log("Token", fcmToken);
        return admin.messaging.sendToDevice(fcmToken, payload).then((response) => {
            console.log("Successfully sent", response);
        }).catch((error) => {
            console.log("error occured", error);
        });
    });
});

exports.sendChallengeMessage = functions.database.ref('/Challenges/{challengeId}').onCreate((snapshot, context) => {
    console.log("sendChallengeMessage function invoked" + snapshot.val().status);
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
