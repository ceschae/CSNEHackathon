import React from 'react';
import ReactDOM from 'react-dom';
import './index.css';
import App from './App';
import constants from './components/constants'
import registerServiceWorker from './registerServiceWorker';

import firebase from 'firebase/app';
import 'firebase/database';

var config = {
    apiKey: constants.firebase.apiKey,
    authDomain: constants.firebase.authDomain,
    databaseURL: constants.firebase.databaseURL,
    projectId: constants.firebase.projectId,
    storageBucket: constants.firebase.storageBucket,
    messagingSenderId: constants.firebase.messagingSenderId
  };
firebase.initializeApp(config);

ReactDOM.render(<App />, document.getElementById('root'));
registerServiceWorker();