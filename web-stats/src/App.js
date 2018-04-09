import React, { Component } from 'react';
import logo from './logo.svg';
import './App.css';

import firebase from 'firebase/app';
import 'firebase/database';

class App extends Component {
    constructor(props) {
        super(props);
        this.state = {
            err: ""
        }
    }

    render() {

        firebase.database().ref("word-stats")
            .once("value")
            .then(snapShot => {
                this.setState({wordsRef: snapShot});
            });

        if(!this.state.wordsRef) {
            return <div>Loading... Please be patient</div>;
        }

        let words = [];
        this.state.wordsRef.forEach(wordSnapshot => {
            (wordSnapshot.val().average < 0.8) ?
            words.push(
                <li class="list-group-item"><b>{wordSnapshot.val().word}</b>: {wordSnapshot.val().average}</li>
            )
            : undefined
        });

        return (
            <div className="App">
                <div class="jumbotron">
                    <h1 class="display-4">Voicebox</h1>
                    <p class="lead">Speech Within Reach</p>
                    <hr class="my-4" />
                    <img src="http://www.csne-erc.org/sites/default/files/NSF-CSNE-LOGO3.png" alt="CSNE" />
                </div>
                <ul class="list-group">
                    {words}
                </ul>
            </div>
        );
    }
}

export default App;
