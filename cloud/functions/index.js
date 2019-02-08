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
        const challengeKey = post.challengeKey;
        const likes1 = post.likes;
        const likes2 = post.likes2;
        var count1, count2;
        if (likes1 == null) {
            count1 = 0;
        } else {
            count1 = Object.keys(likes1).length;
        }

        if (likes2 == null) {
            count2 = 0;
        } else {
            count2 = Object.keys(likes2).length;
        }

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
        if (winner == null) {
            console.log("Its a draw");
            winner = "tie";
        } else {
            console.log("winner" + winner, "loser" + loser);
        }
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
                var result;
                if (userUid == winner) {
                    newPoints = pandaPoints + 5;
                    result = "win";
                } else if (userUid == loser) {
                    newPoints = pandaPoints - 2;
                    result = "lose";
                } else {
                    newPoints = pandaPoints + 2;
                    result = "draw";
                }
                var promise = userSnap.ref.update({ panda_points: newPoints }).then(() => {
                    return admin.database().ref('/token/' + userUid).once('value').then((tokenSnap) => {
                        var fcmToken = tokenSnap.val();
                        if (fcmToken == null) {
                            return null;
                        }
                        var payload;
                        if (result == "win") {
                            payload = {
                                data: {
                                    type: "RESULTS",
                                    winner: winner,
                                    loser: loser,
                                    status: "win",
                                    postKey: postKey
                                }
                            };
                        } else if (result == "lose") {
                            payload = {
                                data: {
                                    type: "RESULTS",
                                    winner: winner,
                                    loser: loser,
                                    status: "lose",
                                    postKey: postKey
                                }
                            };
                        } else if (result == "draw") {
                            var anotherUid;
                            if (userUid == userUid1) {
                                anotherUid = userUid2;
                            } else if (userUid == userUid2) {
                                anotherUid = userUid1;
                            }
                            payload = {
                                data: {
                                    type: "RESULTS",
                                    user1: userUid,
                                    user2: anotherUid,
                                    status: "draw",
                                    postKey: postKey
                                }
                            };
                        }

                        return admin.messaging().sendToDevice(fcmToken, payload).then((response) => {
                            console.log("Successfully sent message to " + userUid, response);
                        });
                    });
                });
                p.push(promise);
            });
            return Promise.all(p).then(() => {
                console.log("imp tasks started", challengeKey, postSnapshot.ref);
                var Pr = [];
                var pr1 = admin.database().ref("/Posts/" + postKey).update({ winner: winner });
                var pr2 = admin.database().ref("/Posts/" + postKey).update({ status: "INACTIVE" });
                var pr3 = admin.database().ref("/Challenges/" + challengeKey).remove();
                var pr4 = admin.database().ref("/User_Challenges/" + userUid1).child(challengeKey).remove();
                var pr5 = admin.database().ref("/User_Challenges/" + userUid2).child(challengeKey).remove();
                Pr.push(pr1, pr2, pr3, pr4, pr5);
                return Promise.all(Pr);
            });
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
        if (fcmToken == null) {
            return null;
        }
        console.log("Token", fcmToken);
        var payload;
        var p = [];
        var promise;
        if (status == "ACCEPTED") {
            const postKey = challenge.postKey;
            payload = {
                data: {
                    challengedUserUid: challengedUserUid,
                    status: status,
                    postKey: postKey,
                    type: "Challenge"
                }
            };


        } else if (status == "REJECTED") {
            payload = {
                data: {
                    challengedUserUid: challengedUserUid,
                    status: status,
                    type: "Challenge"
                }
            };
            promise = datasnapshot.ref.remove();
            p.push(promise);

            promise = admin.database().ref("/User_Challenges/" + challengedUserUid).child(challengeKey).remove();
            p.push(promise);

            promise = admin.database().ref("/User_Challenges/" + challengerUserUid).child(challengeKey).remove();
            p.push(promise);
        }
        promise = admin.messaging().sendToDevice(fcmToken, payload).then((response) => {
            console.log("Successfully sent", response);
        }).catch((error) => {
            console.log("error occured", error);
        });
        p.push(promise);
        return Promise.all(p);
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
        if (fcmToken == null) {
            return null;
        }
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

exports.unfollowed = functions.database.ref('followers/{userUid}/{followingUserUid}')
    .onDelete((_snap, context) => {
        console.log("unfollow triggered");
        const followedUserUid = context.params.userUid;
        return admin.database().ref("/users/" + followedUserUid).once('value').then((userSnap) => {
            var user = userSnap.val();
            var panda_points = user.panda_points;
            var newPoints = panda_points - 1;
            return userSnap.ref.update({ panda_points: newPoints });
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
                    if (fcmToken == null) {
                        return null;
                    }
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
