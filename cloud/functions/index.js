const functions = require('firebase-functions');
const admin = require('firebase-admin');

admin.initializeApp(functions.config().firebase);

exports.calculateResults = functions.database.ref("/Posts/{postId}").onUpdate((change) => {
    console.log("ResultMessage Invoked");

    const postSnapshot = change.after;
    const post = postSnapshot.val();
    const status = post.status;
    if (status == "AWAITING_RESULT") {
        const userUid1 = post.user_id;
        const userUid2 = post.user_id2;
        const postKey = post.postKey;
        const likes1 = post.likes;
        const likes2 = post.likes2;
        const count1 = Object.keys(likes1).length;
        const count2 = Object.keys(likes2).length;

        console.log("likes1" + count1, userUid1);
        console.log("likes2" + count2, userUid2);

        var winner, loser;
        if (count1 > count2) {
            winner = userUid1;
            loser = userUid2;
        } else if (count2 > count1) {
            winner = userUid2;
            loser = userUid1;
        }
        console.log("winner" + winner, "loser" + loser);

        var promises = [];
        var p1 = admin.database().ref("/users/" + userUid1).once('value');
        var p2 = admin.database().ref("/users/" + userUid2).once('value');
        promises.push(p1, p2);
        return Promise.all(promises).then((userSnapshots) => {
            var p = [];
            userSnapshots.forEach((userSnap) => {
                console.log("usersnap", userSnap);
                var user = userSnap.val();
                var userUid = user.user_id;
                var pandaPoints = user.panda_points;
                var newPoints;
                if (userUid == winner) {
                    newPoints = pandaPoints + 5;
                } else if (userUid == loser) {
                    newPoints = pandaPoints - 2;
                } else {
                    newPoints = pandaPoints + 2;
                }
                var promise = userSnap.ref.update({ panda_points: newPoints }).then(() => {
                    return admin.database().ref('/token/' + userUid).once('value').then((tokenSnap) => {
                        var fcmToken = tokenSnap.val();
                        if (fcmToken == null) {
                            return null;
                        }
                        var payload;
                        if (userUid == winner) {
                            payload = {
                                data: {
                                    type: "RESULTS",
                                    winner: winner,
                                    loser: loser,
                                    status: "win",
                                    postKey: postKey
                                }
                            };
                        } else if (userUid = loser) {
                            payload = {
                                data: {
                                    type: "RESULTS",
                                    winner: winner,
                                    loser: loser,
                                    status: "lose",
                                    postKey: postKey
                                }
                            };
                        } else {
                            if (userUid == userUid1) {
                                payload = {
                                    data: {
                                        type: "RESULTS",
                                        user1: userUid1,
                                        user2: userUid2,
                                        status: "draw",
                                        postKey: postKey
                                    }
                                };
                            } else if (userUid == userUid2) {
                                payload = {
                                    data: {
                                        type: "RESULTS",
                                        user1: userUid2,
                                        user2: userUid1,
                                        status: "draw",
                                        postKey: postKey
                                    }
                                };
                            }
                        }

                        return admin.messaging().sendToDevice(fcmToken, payload).then((response) => {
                            console.log("Successfully sent message to " + userUid, response);
                        });
                    });
                });
                p.push(promise);
            });
            return Promise.all(p);
        })
            .then(() => {
                var Pr = [];
                var pr1 = postSnapshot.ref.update({ winner: winner });
                var pr2 = postSnapshot.ref.update({ status: "INACTIVE" });
                Pr.push(pr1, pr2);
                return Promises.all(Pr);
            });
    } else {
        return null;
    }
});


exports.sendMessage = functions.database.ref('/Challenges/{challengeId}').onUpdate((change, context) => {
    console.log("update triggered");
    const datasnapshot = change.after;
    const challenge = datasnapshot.val();
    const status = challenge.status;
    const challengedUserUid = challenge.challengedUserUid;
    const challengerUserUid = challenge.challengerUserUid;

    return admin.database().ref("/token/" + challengerUserUid).once('value').then((snap) => {
        const fcmToken = snap.val();
        console.log("Token", fcmToken);
        var payload;
        if (status == "ACCEPTED") {
            const postKey = challenge.postKey;
            payload = {
                data: {
                    challengedUserUid: challengedUserUid,
                    status: status,
                    postKey: postKey,
                    type: "Challenge"
                }
            }
        } else if (status == "REJECTED") {
            payload = {
                data: {
                    challengedUserUid: challengedUserUid,
                    status: status,
                    type: "Challenge"
                }
            }
        }
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
        return admin.database().ref("/users/" + followedUserUid).once('value').then((userSnap) => {
            var user = userSnap.val();
            var panda_points = user.panda_points;
            var newPoints = panda_points + 1;
            return userSnap.ref.update({ panda_points: newPoints }).then(() => {
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

        });
    });
