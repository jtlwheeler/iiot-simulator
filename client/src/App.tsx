import axios from 'axios';
import * as React from 'react';

interface IState {
    valveState: number
}

class App extends React.Component<any, IState> {

    constructor(props: any) {
        super(props);
        this.state = {
            valveState: 0
        };

        this.getValveInfo = this.getValveInfo.bind(this);
    }

    public async componentDidMount() {
        await this.getValveInfo();
    }

    public render() {
        return (
            <div className="App">
                <header className="App-header">
                    <h1 className="App-title">Welcome to React</h1>
                </header>

                <div>
                    <h2>Valve Status</h2>
                    <h3 className="valve-status">{this.state.valveState}</h3>
                </div>
            </div>
        );
    }

    private async getValveInfo() {
        const response = await axios.get("http://localhost:8080/api/valve");

        this.setState({valveState: response.data.valveState});
    }
}

export default App;
