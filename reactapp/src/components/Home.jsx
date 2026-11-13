import React from 'react';
import {Link} from 'react-router-dom';
function Home(){return <section className="home"><h2>Welcome to the Vegan Snacks App!</h2><p>Explore a delightful range of vegan snacks! Enjoy tasty, guilt-free treats made from natural ingredients.</p><Link className="primary-button" to="/apply">Add Your Vegan Snack</Link></section>;}
export default Home;