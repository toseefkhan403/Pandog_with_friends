const functions = require('firebase-functions');
const admin = require('firebase-admin');

admin.initializeApp(functions.config().firebase);

exports.setLevels = functions.database.ref("/users/{user_uid}/panda_points")
    .onWrite(() => {

    return admin.database().ref("/lastLevelledTime").once("value").then((timeSnap) => {
        var lastLevelledTime = (timeSnap.exists()) ? timeSnap.val() : 0;
        var currentTime = new Date().getTime();
        if ((currentTime - lastLevelledTime) > 3600000) {
            return admin.database().ref("/lastLevelledTime").set(currentTime).then(() => {
                return admin.database().ref("/users/").orderByChild("panda_points").once("value").then((userSnaps) => {
                    console.log("Succesfully got userData");
                    var userCount = userSnaps.numChildren();
                    var userPerLevel = parseInt(userCount / 5);
                    var extraUsers = userCount - (5 * userPerLevel);
                    var currentCount = 0;
                    console.log(userCount, userPerLevel);
                    var promises = [];
                    var promise;
                    userSnaps.forEach((userSnap) => {
                        if (currentCount <= (1 * userPerLevel + extraUsers)) {
                            console.log("GREY", userSnap.val().panda_points);
                            promise = userSnap.ref.child("level").set("GREY");
                        } else if (currentCount <= (2 * userPerLevel + extraUsers)) {
                            console.log("GREEN", userSnap.val().panda_points);
                            promise = userSnap.ref.child("level").set("GREEN");
                        } else if (currentCount <= (3 * userPerLevel + extraUsers)) {
                            console.log("BLUE", userSnap.val().panda_points);
                            promise = userSnap.ref.child("level").set("BLUE");
                        } else if (currentCount <= (4 * userPerLevel + extraUsers)) {
                            console.log("RED", userSnap.val().panda_points);
                            promise = userSnap.ref.child("level").set("PURPLE");
                        } else if (currentCount <= (5 * userPerLevel + extraUsers)) {
                            console.log("BLACK", userSnap.val().panda_points);
                            promise = userSnap.ref.child("level").set("BLACK");
                        }
                        currentCount++;
                        promises.push(promise);
                    });
                    return Promise.all(promises);
                });
            });
        } else {
            return null;
        }
    });
});

exports.calculateResults = functions.database.ref("/Posts/{postId}")
    .onUpdate((change) => {

    const postupdated = change.after;
    const postoutdated = change.before;
    const postAfter = postupdated.val();
    const postBefore = postoutdated.val();
    const postKey = postAfter.postKey;
    console.log("postBefore: " + postBefore, "postAfter: " + postAfter);

    const Pstatus = postAfter.status;
    if (Pstatus === "AWAITING_RESULT") {
        const challenge_id = postAfter.challenge_id;
        const userUid1 = postAfter.user_id;
        const userUid2 = postAfter.user_id2;
        const likes1 = postAfter.likes;
        const likes2 = postAfter.likes2;

        const likesnumber1 = (likes1 == null) ? 0 : Object.keys(likes1);
        const likesnumber2 = (likes2 == null) ? 0 : Object.keys(likes2);

        var status;
        var winnerUid, loserUid;
        if (likesnumber1 == likesnumber2) {
            status = "draw";
            console.log("It's a draw");
        } else {
            status = "notDraw"
            winnerUid = (likesnumber1 > likesnumber2) ? userUid1 : userUid2;
            loserUid = (likesnumber1 > likesnumber2) ? userUid2 : userUid1;
            console.log("winner: " + winnerUid);
            console.log("loser: " + loserUid);
        }

        var userPromises = [];
        var userP1 = admin.database().ref("/users/" + userUid1).once("value");
        var userP2 = admin.database().ref("/users/" + userUid2).once("value");
        userPromises.push(userP1, userP2);
        return Promise.all(userPromises).then((userSnapshots) => {
            var promises = [];
            userSnapshots.forEach((userSnapshot) => {
                var user = userSnapshot.val();
                console.log("Userdata: " + user);
                var userUid = user.user_id;
                console.log("updating points");
                var promise = userSnapshot.ref.child("panda_points").transaction((panda_points) => {
                    if (status === "draw") {
                        panda_points = panda_points + 2;
                    } else {
                        panda_points = (userUid == winnerUid) ? panda_points + 5 : panda_points - 2;
                    }
                    return panda_points;
                })
                    .then(() => {
                        console.log("getting token");
                        return admin.database().ref("/token/" + userUid).once("value").then((tokenSnap) => {
                            var token = tokenSnap.val();
                            if (token == null) {
                                console.log("null token");
                                return null;
                            }
                            var payload;
                            if (status == "draw") {
                                var anotherUid = (userUid == userUid1) ? userUid2 : userUid1;
                                payload = {
                                    data: {
                                        type: "RESULTS",
                                        user1: userUid,
                                        user2: anotherUid,
                                        status: "draw",
                                        postKey: postKey
                                    }
                                };
                            } else {
                                payload = (userUid == winnerUid) ? {
                                    data: {
                                        type: "RESULTS",
                                        winner: winnerUid,
                                        loser: loserUid,
                                        status: "win",
                                        postKey: postKey
                                    }
                                } :
                                    {
                                        data: {
                                            type: "RESULTS",
                                            winner: winnerUid,
                                            loser: loserUid,
                                            status: "lose",
                                            postKey: postKey
                                        }
                                    };
                            }
                            return admin.messaging().sendToDevice(token, payload).then((response) => {
                                console.log("Successfully sent Result message to " + userUid, response);
                                return admin.database().ref("user_notif").child(userUid).push().set(payload);
                            });
                        });

                    });
                promises.push(promise);
            });
            return Promise.all(promises).then(() => {
                console.log("imp tasks started", challenge_id);
                var Pr = [];
                var pr1 = admin.database().ref("/Posts/" + postKey).update({ winner: winnerUid, status: "INACTIVE" });
                var pr2 = admin.database().ref("/Challenges/" + challenge_id).remove();
                var pr3 = admin.database().ref("/User_Challenges/" + userUid1).child(challenge_id).remove();
                var pr4 = admin.database().ref("/User_Challenges/" + userUid2).child(challenge_id).remove();
                Pr.push(pr1, pr2, pr3, pr4);
                return Promise.all(Pr);
            });
        });

    } else {
        return null;
    }
});

exports.sendMessage = functions.database.ref('/Challenges/{challengeId}')
    .onUpdate((change, context) => {
    console.log("update triggered");
    const datasnapshot = change.after;
    const challenge = datasnapshot.val();
    const status = challenge.status;
    const challengedUserUid = challenge.challengedUserUid;
    const challengerUserUid = challenge.challengerUserUid;
    const challengeKey = challenge.challengeKey;
    return admin.database().ref("/token/" + challengerUserUid).once('value').then((snap) => {
        const fcmToken = snap.val();
        if (fcmToken == null) {
            console.log("null token");
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

        promise = admin.database().ref("user_notif").child(challengerUserUid).push().set(payload);
        p.push(promise);

        return Promise.all(p);
    });
});

exports.sendChallengeMessage = functions.database.ref('/Challenges/{challengeId}')
    .onCreate((snapshot, context) => {
    console.log("sendChallengeMessage function invoked" + snapshot.val().status);
    const challenge = snapshot.val();
    var challengerUserUid = challenge.challengerUserUid;
    var challengedUserUid = challenge.challengedUserUid;
    var status = challenge.status;

    return admin.database().ref("/token/" + challengedUserUid).once('value').then((snap) => {
        const fcmToken = snap.val();
        if (fcmToken == null) {
            console.log("null token");
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
        var p = [];
        var promise = admin.messaging().sendToDevice(fcmToken, payload).then((response) => {
            console.log("Sucessfully sent", response);
        }).catch((error) => {
            console.log("Error occured", error);
        });

        p.push(promise);

        promise = admin.database().ref("user_notif").child(challengedUserUid).push().set(payload);
        p.push(promise);
        return Promise.all(p);
    })
        .catch((error) => {
            console.log("Error", error);
        });
});

exports.unfollowed = functions.database.ref('followers/{userUid}/{followingUserUid}')
    .onDelete((_snap, context) => {
        console.log("unfollow triggered");
        const followedUserUid = context.params.userUid;
        return admin.database().ref("/users/" + followedUserUid).child("panda_points").transaction((panda_points) => {
            return panda_points - 1;
        });
    });

exports.sendFollowingMessage = functions.database.ref('/followers/{userUid}/{followingUserUid}')
    .onCreate((snapshot, context) => {
        console.log("sendFollowingMessage function invoked");
        const followedUserUid = context.params.userUid;
        const data = snapshot.val();
        const followingUserUid = data.user_id;

        return admin.database().ref("/users/" + followedUserUid).child("panda_points").transaction((panda_points) => {
            return panda_points + 1;
        })
            .then(() => {
                console.log("retrieving token");
                return admin.database().ref("/token/" + followedUserUid).once('value').then((snap) => {
                    const fcmToken = snap.val();
                    if (fcmToken == null) {
                        console.log("null token");
                        return null;
                    }
                    const payload = {
                        data: {
                            type: "Following",
                            followerUserUid: followingUserUid
                        }
                    };
                    var p = [];
                    var promise = admin.messaging().sendToDevice(fcmToken, payload).then((response) => {
                        console.log("Successfully sent", response);
                    })
                        .catch((error) => {
                            console.log("Following Message MessageLevelError", error);
                        });
                    p.push(promise);

                    promise = admin.database().ref("user_notif").child(followedUserUid).push().set(payload);
                    p.push(promise);

                    return Promise.all(p);
                })
                    .catch((error) => {
                        console.log("Following Message tokenLevel Error", error);
                    });


            });
    });
