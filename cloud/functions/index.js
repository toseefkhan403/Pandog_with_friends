const functions = require('firebase-functions');
const admin = require('firebase-admin');
const express = require('express');
const escapeHtml = require(escape-html);

const firebaseApp = admin.initializeApp(
    functions.config().firebase);

const app = express();

exports.app = functions.https.onRequest(app);

app.get('/', (request, response) => {

});
