import React from 'react';
import ReactDOM from 'react-dom';
import './index.css';
import App from './App';
import constants from './components/constants'
import registerServiceWorker from './registerServiceWorker';

ReactDOM.render(<App />, document.getElementById('root'));
registerServiceWorker();

var config = {
    apiKey: constants.apiKey,
    authDomain: constants.authDomain,
    databaseURL: constants.databaseURL,
    projectId: constants.projectId,
    storageBucket: constants.storageBucket,
    messagingSenderId: constants.messagingSenderId
  };
  firebase.initializeApp(config);