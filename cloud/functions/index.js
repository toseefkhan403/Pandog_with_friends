const functions = require('firebase-functions');
const admin = require('firebase-admin');
const express = require('express');
const escapeHtml = require('escape-html');

const firebaseApp = admin.initializeApp(
    functions.config().firebase);

const app = express();

app.get('/test', (request, response) => {
    var userUid = (request.query.user || request.body.user);
    response.send(userUid);
});

exports.app = functions.https.onRequest((req, res) => {
    res.send('works now');
});

