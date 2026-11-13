import React from 'react';
import {Routes,Route} from 'react-router-dom';
import NavBar from './components/NavBar';
import Home from './components/Home';
import ApplyForm from './components/ApplyForm';
import DisplayVeganSnacks from './components/DisplayVeganSnacks';
import Footer from './components/Footer';
function App(){return <><NavBar/><main><Routes><Route path="/" element={<Home/>}/><Route path="/apply" element={<ApplyForm/>}/><Route path="/getAllVeganSnacks" element={<DisplayVeganSnacks/>}/></Routes></main><Footer/></>;}
export default App;