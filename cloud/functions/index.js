const functions = require('firebase-functions');
const admin = require('firebase-admin');

admin.initializeApp(functions.config().firebase);


exports.limitNotifs = functions.database.ref("/user_notif/{userUid}/{notifKey}")
    .onCreate((snapshot) => {
        console.log("limitNotifs invoked");
        return snapshot.ref.parent.orderByChild("data/time").once("value").then((notifSnaps) => {
            var Notifcount = notifSnaps.numChildren();
            var limit = 25;
            if (Notifcount > limit) {
                var extranotifCount = Notifcount - limit;
                var count = 0;
                var p = [];
                var promise;
                notifSnaps.forEach((NotifSnap) => {
                    if (count < extranotifCount) {
                        promise = NotifSnap.ref.remove();
                        p.push(promise);
                        count = count + 1;
                    }
                });
                return Promise.all(p);
            } else {
                console.log("No limitation required");
                return null;
            }
        });
    });

exports.setLevels = functions.pubsub.topic('set-levels')
    .onPublish(() => {
        console.log("Set-levels invoked");
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

                    promise = userSnap.ref.child("level").set("GREY");
                } else if (currentCount <= (2 * userPerLevel + extraUsers)) {

                    promise = userSnap.ref.child("level").set("GREEN");
                } else if (currentCount <= (3 * userPerLevel + extraUsers)) {

                    promise = userSnap.ref.child("level").set("BLUE");
                } else if (currentCount <= (4 * userPerLevel + extraUsers)) {

                    promise = userSnap.ref.child("level").set("PURPLE");
                } else if (currentCount <= (5 * userPerLevel + extraUsers)) {

                    promise = userSnap.ref.child("level").set("BLACK");
                }
                currentCount++;
                promises.push(promise);
            });
            return Promise.all(promises);

        });


    });


exports.sendTopPostNotif = functions.pubsub.topic('TopPost-Notif')
    .onPublish(() => {
        console.log("Sending TopPost Notif");
        return admin.database().ref("/Posts/").orderByChild("{postId}/status").equalTo("ACTIVE").once("value").then((postSnaps) => {
            var TopPostKey, topPostLikes = 0;
            postSnaps.forEach((postSnap) => {
                var Post = postSnap.val();
                var Likes1 = Post.likes;
                var Likes2 = Post.likes2;
                var likesnumber1 = (likes1 == null) ? 0 : Object.keys(Likes1).length;
                var likesnumber2 = (likes2 == null) ? 0 : Object.keys(Likes2).length;
                var totalLikes = likesnumber1 + likesnumber2;
                TopPostKey = (totalLikes > topPostLikes) ? Post.postKey : TopPostKey;
            });
            return admin.database().ref("/token").once("value").then((tokenSnaps) => {
                var tokens = [];
                tokenSnaps.forEach((tokenSnap) => {
                    var token = tokenSnap.val();
                    tokens.push(token);
                });
                var time = "" + new Date().getTime();
                var payload = {
                    data: {
                        type: "topPost",
                        postKey: TopPostKey,
                        time:time
                    }
                };

                return admin().messaging().sendToDevice(tokens, payload);
            });
        })
    });


exports.calculateResults = functions.database.ref("/Posts/{postId}/status")
    .onUpdate((change, context) => {

        const update = change.after;
        const Pstatus = update.val()

        if (Pstatus === "AWAITING_RESULT") {
            const postKey = context.params.postId;
            return admin.database().ref("/Posts/" + postKey).once("value").then((postSnap) => {

                const postAfter = postSnap.val();
                const challenge_id = postAfter.challenge_id;
                const userUid1 = postAfter.user_id;
                const userUid2 = postAfter.user_id2;
                const likes1 = postAfter.likes;
                const likes2 = postAfter.likes2;

                const likesnumber1 = (likes1 == null) ? 0 : Object.keys(likes1).length;
                const likesnumber2 = (likes2 == null) ? 0 : Object.keys(likes2).length;

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

                                    var payload;
                                    var time = "" + new Date().getTime();
                                    if (status == "draw") {
                                        var anotherUid = (userUid == userUid1) ? userUid2 : userUid1;
                                        payload = {
                                            data: {
                                                type: "RESULTS",
                                                user1: userUid,
                                                user2: anotherUid,
                                                status: "draw",
                                                postKey: postKey,
                                                time: time
                                            }
                                        };
                                    } else {
                                        payload = (userUid == winnerUid) ? {
                                            data: {
                                                type: "RESULTS",
                                                winner: winnerUid,
                                                loser: loserUid,
                                                status: "win",
                                                postKey: postKey,
                                                time: time
                                            }
                                        } :
                                            {
                                                data: {
                                                    type: "RESULTS",
                                                    winner: winnerUid,
                                                    loser: loserUid,
                                                    status: "lose",
                                                    postKey: postKey,
                                                    time: time
                                                }
                                            };
                                    }
                                    return admin.database().ref("/user_notif/" + userUid).push().set(payload).then(() => {
                                        if (token == null) {
                                            console.log("null token");
                                            return null;
                                        } else {
                                            return admin.messaging().sendToDevice(token, payload);
                                        }
                                    });

                                });

                            });
                        promises.push(promise);
                    });
                    return Promise.all(promises).then(() => {
                        console.log("imp tasks started", challenge_id);
                        var Pr = [];
                        var pr1 = (status == "draw") ? admin.database().ref("/Posts/" + postKey).update({ winner: "tie", status: "INACTIVE" }) :
                            admin.database().ref("/Posts/" + postKey).update({ winner: winnerUid, status: "INACTIVE" });
                        var pr2 = admin.database().ref("/Challenges/" + challenge_id).remove();
                        var pr3 = admin.database().ref("/User_Challenges/" + userUid1).child(challenge_id).remove();
                        var pr4 = admin.database().ref("/User_Challenges/" + userUid2).child(challenge_id).remove();
                        Pr.push(pr1, pr2, pr3, pr4);
                        return Promise.all(Pr);
                    });
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

            console.log("Token", fcmToken);
            var payload;
            var time = "" + new Date().getTime();
            var p = [];
            var promise;
            if (status == "ACCEPTED") {
                const postKey = challenge.postKey;
                payload = {
                    data: {
                        challengedUserUid: challengedUserUid,
                        status: status,
                        postKey: postKey,
                        type: "Challenge",
                        time: time
                    }
                };


            } else if (status == "REJECTED") {
                payload = {
                    data: {
                        challengedUserUid: challengedUserUid,
                        status: status,
                        type: "Challenge",
                        time: time
                    }
                };
                promise = datasnapshot.ref.remove();
                p.push(promise);

                promise = admin.database().ref("/User_Challenges/" + challengedUserUid).child(challengeKey).remove();
                p.push(promise);

                promise = admin.database().ref("/User_Challenges/" + challengerUserUid).child(challengeKey).remove();
                p.push(promise);
            }
            if (fcmToken != null) {
                promise = admin.messaging().sendToDevice(fcmToken, payload).then((response) => {
                    console.log("Successfully sent", response);
                }).catch((error) => {
                    console.log("error occured", error);
                });
                p.push(promise);
            } else {
                console.log("null Token");
            }

            promise = admin.database().ref("/user_notif").child(challengerUserUid).push().set(payload);
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

            console.log("Token:", fcmToken);
            var time = "" + new Date().getTime();
            const payload = {
                data: {
                    challengerUserUid: challengerUserUid,
                    status: status,
                    type: "Challenge",
                    time: time
                }
            };
            var p = [];
            var promise;
            if (fcmToken == null) {
                console.log("null token");

            } else {
                promise = admin.messaging().sendToDevice(fcmToken, payload).then((response) => {
                    console.log("Sucessfully sent", response);
                }).catch((error) => {
                    console.log("Error occured", error);
                });
            }

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

exports.sendCommentMessage = functions.database.ref('/Posts/{postId}/comments/{commentId}')
    .onCreate((snapshot, context) => {
        console.log("commentMessage Invoked");
        const postKey = context.params.postId;
        console.log("postKey:", postKey);
        var comment = snapshot.val();
        var userUid = comment.user_id;
        var p = [];
        var promise1 = snapshot.ref.parent.parent.child("user_id").once("value");
        var promise2 = snapshot.ref.parent.parent.child("user_id2").once("value");
        p.push(promise1, promise2);

        return Promise.all(p).then((uidSnapshots) => {
            var promises = [];
            var promise;
            uidSnapshots.forEach((uidSnapshot) => {
                var uid = uidSnapshot.val();
                if (uid != userUid) {
                    promise = admin.database().ref("/token/" + uid).once("value").then((tokenSnap) => {
                        var token = tokenSnap.val();

                        var payload;
                        var time = "" + new Date().getTime();
                        payload = {
                            data: {
                                type: "comment",
                                postKey: postKey,
                                userUid: userUid,
                                time: time
                            }
                        };

                        return admin.database().ref("/user_notif/" + uid).push().set(payload).then(() => {
                            if (token == null) {
                                console.log("nullToken");
                                return null;
                            } else {
                                return admin.messaging().sendToDevice(token, payload);
                            }

                        });
                        promises.push(promise);
                    });
                }
            });
            return Promise.all(promises);
        });
    });

exports.sendCommentMentionedMessage = functions.database.ref("/Posts/{postId}/comments/{commentId}/mentionArrayList/{index}")
    .onCreate((snapshot, context) => {
        console.log("commentMentionedMessage invoked");
        var mention = snapshot.val();
        var mentionedUid = mention.mentionUid;
        const postkey = context.params.postId;
        console.log("postKey:", postkey);
        return snapshot.ref.parent.parent.child("user_id").once("value").then((uidSnapshot) => {
            var uid = uidSnapshot.val();
            return admin.database().ref("/token/" + mentionedUid).once("value").then((tokenSnap) => {
                var token = tokenSnap.val();

                var time = "" + new Date().getTime();
                var payload = {
                    data: {
                        type: "mention",
                        mentionedPlace: "comment",
                        userUid: uid,
                        postKey: postkey,
                        time: time
                    }
                };
                return admin.database().ref("/user_notif/" + mentionedUid).push().set(payload).then(() => {
                    if (token == null) {
                        console.log("nullToken");
                        return null;
                    } else {
                        return admin.messaging().sendToDevice(token, payload);
                    }
                });
            });
        });
    });

exports.sendPostMentionedMessage = functions.database.ref("/Posts/{postId}/mention_hash_map/{userUid}/{index}")
    .onCreate((snapShot, context) => {
        console.log("Post Mentioned Message Invoked");
        const mention = snapShot.val();
        const postKey = context.params.postId;
        const userUid = context.params.userUid;
        const mentionedUid = mention.mentionUid;
        console.log("mentionedUid:" + mentionedUid, "postKey" + postKey);
        return admin.database().ref("/token/" + mentionedUid).once("value").then((tokenSnap) => {
            var token = tokenSnap.val();
            var time = "" + new Date().getTime();
            var payload = {
                data: {
                    type: "mention",
                    mentionedPlace: "post",
                    userUid: userUid,
                    postKey: postKey,
                    time: time
                }
            };
            return admin.database().ref("/user_notif/" + mentionedUid).push().set(payload).then(() => {
                if (token == null) {
                    console.log("null Token");
                    return null;
                } else {
                    return admin.messaging().sendToDevice(token, payload);
                }
            });
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

                    var time = "" + new Date().getTime();
                    const payload = {
                        data: {
                            type: "Following",
                            followerUserUid: followingUserUid,
                            time: time
                        }
                    };
                    var p = [];
                    var promise;
                    if (fcmToken != null) {
                        promise = admin.messaging().sendToDevice(fcmToken, payload).then((response) => {
                            console.log("Successfully sent", response);
                        })
                            .catch((error) => {
                                console.log("Following Message MessageLevelError", error);
                            });
                        p.push(promise);
                    } else {
                        console.log("null Token");
                    }

                    promise = admin.database().ref("user_notif").child(followedUserUid).push().set(payload);
                    p.push(promise);

                    return Promise.all(p);
                })
                    .catch((error) => {
                        console.log("Following Message tokenLevel Error", error);
                    });


            });
    });
