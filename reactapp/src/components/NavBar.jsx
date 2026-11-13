import React from 'react';
import {Link} from 'react-router-dom';
function NavBar(){return <nav className="navbar"><Link className="brand" to="/">Vegan Snacks Application</Link><ul><li><Link to="/">Home</Link></li><li><Link to="/getAllVeganSnacks">Snacks Details</Link></li></ul></nav>;}
export default NavBar;