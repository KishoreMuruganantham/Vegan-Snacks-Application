import React,{useEffect,useState} from 'react';
import API_URL from '../apiConfig';
function DisplayVeganSnacks(){
 const [snacks,setSnacks]=useState([]); const [error,setError]=useState('');
 useEffect(()=>{fetch(API_URL+'/getAllVeganSnacks',{method:'GET',headers:{'Content-Type':'application/json'}}).then(response=>{if(!response.ok)throw new Error('Unable to fetch vegan snacks.');return response.json();}).then(setSnacks).catch(err=>setError(err.message));},[]);
 return <section className="snacks-container"><h2>Submitted Vegan Snacks</h2>{error&&<p className="error">{error}</p>}<table className="snacks-table"><thead><tr><th>Snack Name</th><th>Snack Type</th><th>Quantity</th><th>Price</th><th>Expiry in Months</th></tr></thead><tbody>{snacks.map(snack=><tr key={snack.id}><td>{snack.snackName}</td><td>{snack.snackType}</td><td>{snack.quantity}</td><td>Rs.{snack.price}</td><td>{snack.expiryInMonths}</td></tr>)}{snacks.length===0&&!error&&<tr><td colSpan="5">No vegan snacks submitted.</td></tr>}</tbody></table></section>;
}
export default DisplayVeganSnacks;