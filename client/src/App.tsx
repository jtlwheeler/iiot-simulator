import axios from 'axios';
import * as React from 'react';

interface IState {
    lastRefresh?: Date
    valveState?: number;
}

class App extends React.Component<any, IState> {

    private interval: NodeJS.Timeout;

    constructor(props: any) {
        super(props);
        this.state = {
            lastRefresh: undefined,
            valveState: undefined
        };

        this.getValveInfo = this.getValveInfo.bind(this);
    }

    public componentDidMount() {
        this.interval = setInterval(async () => await
                this.getValveInfo()
            , 2000);
    }

    public componentWillUnmount() {
        clearInterval(this.interval);
    }

    public render() {
        return (
            <div className="App">
                <header className="App-header">
                    <h1 className="App-title">Welcome to React</h1>
                </header>

                {this.state.valveState !== undefined && this.state.lastRefresh ?
                    <div className="valve-status">
                        <h2>Valve Status</h2>
                        <h3>{this.state.valveState}</h3>
                        <h3>{this.state.lastRefresh.toString()}</h3>
                    </div>
                    : undefined
                }
            </div>
        );
    }

    private async getValveInfo() {
        const response = await axios.get("http://localhost:8080/api/valve");
        this.setState({
            lastRefresh: new Date(),
            valveState: response.data.valveState
        });
    }
}

export default App;
